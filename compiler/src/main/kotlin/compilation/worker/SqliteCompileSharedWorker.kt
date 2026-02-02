package compilation.worker

import compilation.SqliteCompileTimeOptions
import compilation.compiler
import compilation.sharedCompilerFlags
import sqliteSourceFile

/**
 * Compiles a shared library.
 */
abstract class SqliteCompileSharedWorker : SqliteCompileWorker() {

    override fun execute() {
        val params = parameters.compilationParameters.get()
        val sources = parameters.sqliteSourcesDirectory
        val target = parameters.target.get()
        val sourceFile = sqliteSourceFile(sources, parameters.compilationParameters).get().asFile
        val libraryFile = target.libraryFile.get().asFile
        val platform = target.platform.get()
        val compilerFlags = sharedCompilerFlags(params, platform)
        val compiler = compiler(platform.operatingSystem, params)

        execOperations.exec {
            commandLine(
                *compiler,
                *compilerFlags,
                "-o",
                libraryFile.absolutePath,
                sourceFile.absolutePath,
                *SqliteCompileTimeOptions.map { "-D$it" }.toTypedArray(),
                "-O3"
            )
        }
    }
}