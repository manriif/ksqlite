package tasks

import compilation.worker.SqliteCompileStaticWorker
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.submit

/**
 * Task responsible for compiling SQLite and generating static library files for static linking.
 */
@CacheableTask
abstract class SqliteCompileStaticTask : SqliteCompileTask() {

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