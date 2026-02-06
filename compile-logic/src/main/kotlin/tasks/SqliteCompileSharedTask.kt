package tasks

import compilation.worker.SqliteCompileSharedWorker
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.submit

/**
 * Task responsible for compiling SQLite and generating shared library files for dynamic linking.
 */
@CacheableTask
abstract class SqliteCompileSharedTask : SqliteCompileTask() {

    @TaskAction
    fun compile() {
        val workQueue = workerExecutor.noIsolation()

        let { task ->
            targets.get().forEach { target ->
                workQueue.submit(SqliteCompileSharedWorker::class) {
                    this.compilationParameters = task.compilationParameters
                    this.sqliteSourcesDirectory = task.sqliteSourcesDirectory
                    this.target = target
                }
            }
        }

        workQueue.await()
    }
}