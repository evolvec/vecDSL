package org.ftn.vecDSL.generator.oracledb

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
 * Oracle AI Database 26ai target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/oracledb/python, typescript, java) and writes a Docker Compose file for Oracle AI Database Free with its setup
 * script (generated/oracledb/docker-compose.yml, setup/). Python uses python-oracledb, TypeScript node-oracledb, both in thin
 * mode, and Java the JDBC API (java.sql) with the Oracle JDBC thin driver at run time.
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's 'language' hint,
 * otherwise all.
 */
class OracledbGenerator implements TargetGenerator {

	public static val VENDOR = OracledbPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = OracledbManifest.load

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
		fsa.generateFile('setup/01-vecdsl-user.sh', VENDOR, manifest.render('environment/setup/01-vecdsl-user.sh'))
	}

	def List<OracledbPrinter> printers(Script script) {
		val List<OracledbPrinter> all = #[new OracledbPython, new OracledbTypeScript, new OracledbJava]
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
		val statements = new OracledbStatements(script)
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
	 * The default Easy Connect string of the program (host:port/service): the connection's uri, or one built from host, port and
	 * the service (database-name, else FREEPDB1); the user is the connection's username, else VECDSL. The password never enters
	 * the program; it comes from the environment.
	 */
	def private Program.Connection connection(Script script) {
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultDsn = manifest.get('dsn')
		if (declared === null) return new Program.Connection(defaultDsn, null, manifest.get('user'), null, null, null)
		val password = if (declared.password instanceof EnvRef) Literals.of(declared.password) else null
		val dsn = if (declared.uri !== null) {
			dsnOf(declared.uri) ?: defaultDsn
		} else if (declared.host !== null || declared.databaseName !== null || declared.port > 0) {
			(declared.host ?: '127.0.0.1') + ':' + (if (declared.port > 0) declared.port else manifest.get('server.port')) + '/'
				+ (declared.databaseName ?: manifest.get('service'))
		} else {
			defaultDsn
		}
		val timeout = if (ModelUtil.written(declared, 'timeoutMillis')) declared.timeoutMillis / 1000.0 else null
		new Program.Connection(dsn, null, declared.username ?: manifest.get('user'), password, declared.databaseName, timeout)
	}

	/** An Easy Connect string from a uri (oracle://host:port/service or host:port/service), or null for another form. */
	def static String dsnOf(String uri) {
		val plain = if (uri.startsWith('oracle://')) uri.substring('oracle://'.length) else uri
		if (plain.contains('@') || plain.contains('://') || plain.contains('?')) null else plain
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
