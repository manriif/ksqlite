package compilation

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import sqliteSourceFile
import javax.inject.Inject

/**
 * Compiles a shared library.
 */
abstract class SqliteCompileSharedWorker : WorkAction<SqliteCompileSharedWorker.Parameters> {

    @get:Inject
    protected abstract val fileOperations: FileSystemOperations

    @get:Inject
    protected abstract val execOperations: ExecOperations

    ///////////////////////////////////////////////////////////////////////////
    // Parameters
    ///////////////////////////////////////////////////////////////////////////

    interface Parameters : WorkParameters {
        val compilationParameters: Property<SqliteCompilationParameters>
        val sqliteSourcesDirectory: DirectoryProperty
        val target: Property<SqliteTarget>
    }

    ///////////////////////////////////////////////////////////////////////////
    // Execution
    ///////////////////////////////////////////////////////////////////////////

    override fun execute() {
        val params = parameters.compilationParameters.get()
        val sources = parameters.sqliteSourcesDirectory
        val target = parameters.target.get()

        fileOperations.delete { delete(target.libraryDirectory) }
        target.libraryDirectory.get().asFile.mkdirs()

        val sourceFile = sqliteSourceFile(sources, parameters.compilationParameters).get().asFile
        val libraryFile = target.sharedLibraryFile(parameters.compilationParameters).get().asFile

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