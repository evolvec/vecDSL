package org.ftn.vecDSL.generator.vespa

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
 * Vespa target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/vespa/python, typescript, java) and writes a Docker Compose file for a single Vespa node
 * (generated/vespa/docker-compose.yml). The programs deploy the application packages the generator builds and use the
 * document and query APIs over HTTP: Python with urllib, TypeScript with fetch, Java with java.net.http and Gson.
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class VespaGenerator implements TargetGenerator {

	public static val VENDOR = VespaPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = VespaManifest.load

	override vendor() {
		VENDOR
	}

	override generate(Script script, IFileSystemAccess2 fsa) {
		val program = program(script)
		val configUrl = configUrl(script)
		for (printer : printers(script)) {
			for (file : printer.files(program, manifest, configUrl).entrySet) {
				fsa.generateFile(printer.language + '/' + file.key, VENDOR, file.value)
			}
		}
		fsa.generateFile('docker-compose.yml', VENDOR, manifest.render('environment/docker-compose.yml'))
	}

	def List<VespaPrinter> printers(Script script) {
		val List<VespaPrinter> all = #[new VespaPython, new VespaTypeScript, new VespaJava]
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
		val statements = new VespaStatements(script)
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
	 * The query and document API URL of the program: the connection's uri read by endpoint(), or one built from host and port (https with
	 * tls). Tokens never enter the URL; the runtime reads VECDSL_VESPA_TOKEN from the environment.
	 */
	def private Program.Connection connection(Script script) {
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultUrl = manifest.get('url')
		if (declared === null) return new Program.Connection(defaultUrl, null, null, null, null, null)
		val url = if (declared.uri !== null) {
			endpoint(declared)?.toString ?: declared.uri
		} else if (declared.host !== null || declared.port > 0 || declared.tls) {
			(if (declared.tls) 'https://' else 'http://') + (declared.host ?: '127.0.0.1') + ':' + (if (declared.port > 0) declared.port else manifest.get('query.port'))
		} else {
			defaultUrl
		}
		val seconds = if (ModelUtil.written(declared, 'timeoutMillis')) declared.timeoutMillis / 1000.0 else null
		new Program.Connection(url, null, null, null, null, seconds)
	}

	/** The config server URL: the host of the connection's endpoint or host with the config server port, or the default. */
	def String configUrl(Script script) {
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		if (declared === null || (declared.uri === null && declared.host === null)) return manifest.get('config.url')
		val address = if (declared.uri !== null) endpoint(declared) else null
		val host = if (address !== null) address.host else if (declared.uri === null) declared.host else null
		val scheme = if (address !== null) address.scheme else if (declared.tls) 'https' else 'http'
		if (host === null) return manifest.get('config.url')
		scheme + '://' + host + ':' + manifest.get('config.port')
	}

	/**
	 * The connection's uri as an http or https URL: a uri without a scheme (host:port or host:port/path) is read
	 * as http://<uri>, https with tls. Null when the uri does not parse as a URL with a host or has another scheme; VespaStatements
	 * raises that reason in the connection statement, and the program keeps the uri as written and the default config server URL.
	 */
	def static java.net.URI endpoint(Connection declared) {
		val text = if (declared.uri.matches('[A-Za-z][A-Za-z0-9+.-]*://.*')) declared.uri else (if (declared.tls) 'https://' else 'http://') + declared.uri
		val address = try {
			new java.net.URI(text)
		} catch (java.net.URISyntaxException e) {
			null
		}
		if (address === null || address.host === null || !#['http', 'https'].contains(address.scheme.toLowerCase)) null else address
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
