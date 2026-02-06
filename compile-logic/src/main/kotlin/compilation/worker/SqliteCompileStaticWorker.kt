package compilation.worker

import compilation.SqliteCompileTimeOptions
import compilation.archiver
import compilation.compiler
import compilation.staticCompilerFlags
import sqliteSourceFile
import kotlin.io.path.createTempFile

/**
 * Compiles a static library.
 */
abstract class SqliteCompileStaticWorker : SqliteCompileWorker() {

    override fun execute() {
        val params = parameters.compilationParameters.get()
        val sources = parameters.sqliteSourcesDirectory
        val target = parameters.target.get()
        val sourceFile = sqliteSourceFile(sources, parameters.compilationParameters).get().asFile
        val libraryFile = target.libraryFile.get().asFile
        val objectFile = createTempFile(params.libraryName, libraryFile.extension).toFile()
        val platform = target.platform.get()
        val compilerFlags = staticCompilerFlags(execOperations, params, platform)
        val compiler = compiler(platform.operatingSystem, params)
        val archiver = archiver(platform.operatingSystem, params)

        execOperations.exec {
            commandLine(
                *compiler,
                *compilerFlags,
                "-c",
                sourceFile.absolutePath,
                "-o",
                objectFile.absolutePath,
                *SqliteCompileTimeOptions.map { "-D$it" }.toTypedArray(),
                "-O3"
            )
        }

        execOperations.exec {
            commandLine(
                *archiver,
                "rcs",
                libraryFile.absolutePath,
                objectFile.absolutePath
            )
        }
    }
}