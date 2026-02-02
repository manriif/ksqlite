package tasks

import compilation.SqliteCompilationParameters
import compilation.SqliteTarget
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

/**
 * Task responsible for compiling SQLite and generating library files.
 */
abstract class SqliteCompileTask : DefaultTask() {

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Input
    abstract val compilationParameters: Property<SqliteCompilationParameters>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sqliteSourcesDirectory: DirectoryProperty

    @get:Nested
    abstract val targets: ListProperty<SqliteTarget>

    @Suppress("unused")
    @get:OutputFiles
    internal val outputLibraries: Provider<List<File>> = targets.map { targets ->
        targets.map { it.libraryFile.asFile.get() }
    }
}