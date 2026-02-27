package compilation.worker

import compilation.SqliteDefines
import compilation.compiler
import compilation.sharedCompilerFlags

/**
 * Compiles a shared library.
 */
abstract class SqliteCompileSharedWorker : SqliteCompileWorker() {

    override fun execute() {
        val params = parameters.compilationParameters.get()
        val target = parameters.target.get()
        val libraryFile = target.libraryFile.get().asFile
        val platform = target.platform.get()
        val compilerFlags = sharedCompilerFlags(params, platform)
        val compiler = compiler(platform.operatingSystem, params)

        val sourceFiles = parameters.sourceFiles
            .map { it.absolutePath }
            .toTypedArray()

        execOperations.exec {
            commandLine(
                *compiler,
                *compilerFlags,
                "-o",
                libraryFile.absolutePath,
                *sourceFiles,
                *SqliteDefines.map { "-D$it" }.toTypedArray(),
                "-O3"
            )
        }
    }
}