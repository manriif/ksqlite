package modules

import komple.task.OutputToolTask
import komple.task.hasChanged
import komple.task.clearAndGetAsFile
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Task responsible for compiling SQLite WASM.
 */
@CacheableTask
abstract class WasmCompileTask : OutputToolTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ksqliteDirectory: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sqliteDirectory: DirectoryProperty

    @TaskAction
    fun compile() {
        val tracker = tracker.get()

        if (!tracker.hasChanged()) {
            didWork = false
            return
        }

        compileSqliteWasm(
            fileOperations = fileOperations,
            commandExecutor = newCommandExecutor(),
            ksqliteDirectory = ksqliteDirectory.get().asFile,
            sqliteDirectory = sqliteDirectory.get().asFile,
            outputDirectory = fileOperations.clearAndGetAsFile(outputDirectory)
        )
    }
}