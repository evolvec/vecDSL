package org.ftn.vecDSL.generator.redis

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
 * Redis target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/redis/python, typescript, java) and writes a Docker Compose file for Redis Open Source
 * (generated/redis/docker-compose.yml). Python uses redis-py, TypeScript node-redis, Java Jedis.
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class RedisGenerator implements TargetGenerator {

	public static val VENDOR = RedisPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = RedisManifest.load

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

	def List<RedisPrinter> printers(Script script) {
		val List<RedisPrinter> all = #[new RedisPython, new RedisTypeScript, new RedisJava]
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
		val statements = new RedisStatements(script)
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
	 * The default URL of the program: the connection's uri, or one built from host and port (rediss with tls). Credentials never
	 * enter the URL: the user and password come from the environment (the connection's env(...) reference names the variable).
	 */
	def private Program.Connection connection(Script script) {
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultUrl = manifest.get('url')
		if (declared === null) return new Program.Connection(defaultUrl, null, null, null, null, null)
		val password = if (declared.password instanceof EnvRef) Literals.of(declared.password) else null
		val url = if (declared.uri !== null) {
			declared.uri
		} else if (declared.host !== null || declared.port > 0 || declared.tls) {
			(if (declared.tls) 'rediss://' else 'redis://') + (declared.host ?: '127.0.0.1') + ':' + (if (declared.port > 0) declared.port else manifest.get('server.port'))
		} else {
			defaultUrl
		}
		new Program.Connection(url, null, declared.username, password, null, null)
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
