package org.ftn.vecDSL.generator.mongodb

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
 * MongoDB target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/mongodb/python, typescript, java) and writes a Docker Compose file for MongoDB with mongot (the Atlas Local image,
 * generated/mongodb/docker-compose.yml). Every language uses the official driver: pymongo (Python), mongodb (TypeScript) and
 * mongodb-driver-sync (Java).
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class MongodbGenerator implements TargetGenerator {

	public static val VENDOR = MongodbPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = MongodbManifest.load

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

	def List<MongodbPrinter> printers(Script script) {
		val List<MongodbPrinter> all = #[new MongodbPython, new MongodbTypeScript, new MongodbJava]
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
		val statements = new MongodbStatements(script, manifest.get('database'))
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
	 * The default URI of the program: the connection's uri, or one built from host and port (tls adds tls=true); the user is the
	 * connection's username. Credentials never enter the URI (user information in a declared uri is removed): the generated program reads them from the environment.
	 */
	def private Program.Connection connection(Script script) {
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultUri = manifest.get('uri')
		if (declared === null) return new Program.Connection(defaultUri, null, null, null, null, null)
		val password = if (declared.password instanceof EnvRef) Literals.of(declared.password) else null
		val uri = if (declared.uri !== null) {
			declared.uri.replaceFirst('//[^/@]*@', '//')
		} else if (declared.host !== null) {
			'mongodb://' + declared.host + ':' + (if (declared.port > 0) declared.port else manifest.get('server.port')) + '/?directConnection=true' + (if (declared.tls) '&tls=true' else '')
		} else {
			defaultUri
		}
		val timeout = if (declared.timeoutMillis > 0) Double.valueOf(declared.timeoutMillis / 1000.0) else null
		new Program.Connection(uri, null, declared.username, password, declared.databaseName, timeout)
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
