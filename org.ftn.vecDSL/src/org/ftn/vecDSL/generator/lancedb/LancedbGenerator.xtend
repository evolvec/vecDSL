package org.ftn.vecDSL.generator.lancedb

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
 * LanceDB target (embedded, open source). Maps a script once to the code model, then prints it for every selected host
 * language (generated/lancedb/python, typescript, java) and writes a README on the embedded deployment instead of a
 * Docker Compose file: the library runs inside each program over a directory of Lance files, so there is no server.
 * Python uses the lancedb SDK, TypeScript @lancedb/lancedb, Java the Lance Java SDK (lance-core).
 *
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
class LancedbGenerator implements TargetGenerator {

	public static val VENDOR = LancedbPrinter.VENDOR
	public static val LANGUAGES_PROPERTY = 'vecdsl.languages'

	val manifest = LancedbManifest.load

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

	def List<LancedbPrinter> printers(Script script) {
		val List<LancedbPrinter> all = #[new LancedbPython, new LancedbTypeScript, new LancedbJava]
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
		val statements = new LancedbStatements(script)
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
	 * The storage URI: the connection's path or uri (a directory or an object-store URI), memory:// for in-memory, else
	 * lancedb-data in the working directory. LancedbStatements rejects what an embedded connection cannot carry.
	 */
	def private Program.Connection connection(Script script) {
		val declared = EcoreUtil2.getAllContentsOfType(script, Connection).head
		val defaultUri = manifest.get('storage.uri')
		if (declared === null) return new Program.Connection(defaultUri, null, null, null, null, null)
		val base = if (declared.localPath !== null) declared.localPath else if (declared.uri !== null) declared.uri
			else if (declared.inMemory) 'memory://' else defaultUri
		val uri = if (declared.databaseName === null) base else base.replaceAll('[/\\\\]+$', '') + '/' + declared.databaseName
		new Program.Connection(uri, null, null, null, declared.databaseName, null)
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
