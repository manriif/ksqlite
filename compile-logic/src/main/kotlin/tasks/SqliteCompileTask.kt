package tasks

import compilation.SqliteCompilationParameters
import compilation.SqliteTarget
import compilation.worker.SqliteCompileSharedWorker
import compilation.worker.SqliteCompileStaticWorker
import compilation.worker.SqliteCompileWorker
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import utils.sha256
import java.io.File
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Task responsible for compiling SQLite and generating library files.
 */
abstract class SqliteCompileTask(
    @get:Internal
    private val workerClass: KClass<out SqliteCompileWorker>
) : DefaultTask() {

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Input
    abstract val compilationParameters: Property<SqliteCompilationParameters>

    @get:Internal
    abstract val checksumFile: RegularFileProperty

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

    @TaskAction
    fun compile() {
        executeIfChecksumChanged(checksumFile.get(), outputLibraries.get()::sha256) {
            val workQueue = workerExecutor.noIsolation()

            let { task ->
                targets.get().forEach { target ->
                    workQueue.submit(workerClass) {
                        this.compilationParameters = task.compilationParameters
                        this.sqliteSourcesDirectory = task.sqliteSourcesDirectory
                        this.target = target
                    }
                }
            }

            workQueue.await()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Variants
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Task responsible for compiling SQLite and generating shared library files for dynamic linking.
     */
    @CacheableTask
    abstract class Shared : SqliteCompileTask(SqliteCompileSharedWorker::class)

    /**
     * Task responsible for compiling SQLite and generating static library files for static linking.
     */
    @CacheableTask
    abstract class Static : SqliteCompileTask(SqliteCompileStaticWorker::class)
}