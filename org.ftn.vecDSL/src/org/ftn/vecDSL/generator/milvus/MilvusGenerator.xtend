package org.ftn.vecDSL.generator.milvus

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
import org.ftn.vecDSL.vecDSL.EDeploymentMode
import org.ftn.vecDSL.vecDSL.Method
import org.ftn.vecDSL.vecDSL.Script

/**
 * Milvus target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/milvus/python, typescript, java) and writes a Docker Compose file for a standalone server of
 * the supported version (generated/milvus/docker-compose.yml).
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class MilvusGenerator implements TargetGenerator {

	public static val VENDOR = MilvusPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	/** Emitted constructs that Milvus Lite rejects, with the feature named in the run-time error. */
	static val LITE_GAPS = #[
		'PHRASE_MATCH(' -> 'phrase matching',
		'STL_SORT' -> 'STL_SORT scalar indexes',
		'add_collection_field' -> 'adding fields to an existing collection'
	]

	val manifest = MilvusManifest.load

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

	def List<MilvusPrinter> printers(Script script) {
		val List<MilvusPrinter> all = #[new MilvusPython, new MilvusTypeScript, new MilvusJava]
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
		val statements = new MilvusStatements(script)
		val List<Program.Method> methods = newArrayList
		val Set<String> used = newHashSet
		var index = 0
		for (statement : script.methods) {
			index = index + 1
			for (method : statements.methods(statement, uniqueName(statement, index, used))) {
				methods += new Program.Method(method.name, method.parameters, method.body, method.notes,
					serverFeatures(method.body), method.helper)
			}
		}
		val parameters = script.parameters.map[
			new Program.Parameter(name, type.literal, if (defaultValue === null) null else Literals.of(defaultValue))
		]
		new Program(script.name, script.version, script.description, parameters, connection(script), methods)
	}

	def private Program.Connection connection(Script script) {
		val connection = EcoreUtil2.getAllContentsOfType(script, Connection).head
		if (connection === null) return new Program.Connection(defaultUri, null, null, null, null, null)
		val token = connection.token ?: connection.apiKey
		new Program.Connection(
			uri(connection, script),
			if (token === null) null else Literals.of(token),
			connection.username,
			if (connection.password === null) null else Literals.of(connection.password),
			connection.databaseName,
			if (connection.timeoutMillis > 0) connection.timeoutMillis / 1000.0 else null
		)
	}

	def private String uri(Connection connection, Script script) {
		if (connection.uri !== null) return connection.uri
		if (connection.localPath !== null) return connection.localPath
		if (connection.inMemory || connection.deployment == EDeploymentMode.EMBEDDED) {
			return './' + Syntax.snake(script.name) + '.db'
		}
		if (connection.host !== null) {
			val scheme = if (connection.tls) 'https' else 'http'
			return scheme + '://' + connection.host + ':' + (if (connection.port > 0) String.valueOf(connection.port) else manifest.get('server.port'))
		}
		defaultUri
	}

	def private String defaultUri() {
		'http://localhost:' + manifest.get('server.port')
	}

	def private List<String> serverFeatures(List<Code.Stmt> body) {
		val List<String> found = newArrayList
		Code.visit(body, [ expr |
			val marker = switch expr {
				Code.Filter: expr.template
				Code.Text: expr.value
				Code.Call: expr.op
				default: null
			}
			if (marker !== null) {
				for (gap : LITE_GAPS) {
					if (marker.contains(gap.key) && !found.contains(gap.value)) found += gap.value
				}
			}
		])
		found
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
