package tasks

import compilation.SqliteCompilationParameters
import compilation.SqliteCompileStaticWorker
import compilation.SqliteStaticTarget
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task responsible for compiling SQLite and generating static library files (.a) for static targets.
 * Libraries are compiled concurrently as a compilation is a long-running operation.
 */
@CacheableTask
abstract class SqliteCompileStaticTask : DefaultTask() {

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Input
    abstract val compilationParameters: Property<SqliteCompilationParameters>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sqliteSourcesDirectory: DirectoryProperty

    @get:Nested
    abstract val targets: ListProperty<SqliteStaticTarget>

    @TaskAction
    fun compile() {
        val workQueue = workerExecutor.noIsolation()

        let { task ->
            targets.get().forEach { target ->
                workQueue.submit(SqliteCompileStaticWorker::class) {
                    this.compilationParameters = task.compilationParameters
                    this.sqliteSourcesDirectory = task.sqliteSourcesDirectory
                    this.target = target
                }
            }
        }

        workQueue.await()
    }

}