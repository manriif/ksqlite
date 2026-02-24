package compilation.worker

import compilation.SqliteCompileTimeOptions
import compilation.archiver
import compilation.compiler
import compilation.staticCompilerFlags
import kotlin.io.path.createTempFile

/**
 * Compiles a static library.
 */
abstract class SqliteCompileStaticWorker : SqliteCompileWorker() {

    override fun execute() {
        val params = parameters.compilationParameters.get()
        val target = parameters.target.get()
        val libraryFile = target.libraryFile.get().asFile
        val platform = target.platform.get()
        val compilerFlags = staticCompilerFlags(execOperations, params, platform)
        val compiler = compiler(platform.operatingSystem, params)
        val archiver = archiver(platform.operatingSystem, params)

        val sourceFilesWithObjects = parameters.sourceFiles.files.associateWith { file ->
            createTempFile(
                prefix = "${params.libraryName}${file.nameWithoutExtension}",
                suffix = libraryFile.extension
            ).toFile()
        }

        sourceFilesWithObjects.forEach { (sourceFile, objectFile) ->
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
        }

        val objectFiles = sourceFilesWithObjects.values
            .map { it.absolutePath }
            .toTypedArray()

        execOperations.exec {
            commandLine(
                *archiver,
                "rcs",
                libraryFile.absolutePath,
                *objectFiles
            )
        }
    }
}