package org.ftn.vecDSL.generator.duckdb

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
 * DuckDB target, embedded: DuckDB with the vss and fts extensions runs inside each generated program over one database file.
 * Maps a script once to the code model, then prints it for every selected host language (generated/duckdb/python, typescript,
 * java) and writes a README instead of a Docker Compose file (there is no server). Python uses the duckdb client, TypeScript
 * the Neo client (@duckdb/node-api), Java the DuckDB JDBC driver.
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class DuckdbGenerator implements TargetGenerator {

	public static val VENDOR = DuckdbPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = DuckdbManifest.load

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
		fsa.generateFile('README.md', VENDOR, manifest.render('environment/README.md'))
	}

	def List<DuckdbPrinter> printers(Script script) {
		val List<DuckdbPrinter> all = #[new DuckdbPython, new DuckdbTypeScript, new DuckdbJava]
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
		val statements = new DuckdbStatements(script)
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
	 * The default database file of the program: the connection's local path or uri, :memory: for an in-memory connection, else
	 * vecdsl.duckdb; a relative path is resolved against the program's folder.
	 */
	def private Program.Connection connection(Script script) {
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultPath = manifest.get('storage.path')
		if (declared === null) return new Program.Connection(defaultPath, null, null, null, null, null)
		val path = if (declared.localPath !== null) declared.localPath else if (declared.uri !== null && !declared.uri.matches('[A-Za-z][A-Za-z0-9+.-]+:.*')) declared.uri
			else if (declared.inMemory) ':memory:' else defaultPath
		new Program.Connection(path, null, null, null, declared.databaseName, null)
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
