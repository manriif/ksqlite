package ksqlite.gradle

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

private const val KSQLITE_TASK_GROUP = "ksqlite"

/**
 * Registers a tasks under the Ksqlite group.
 */
internal inline fun <reified T : Task> TaskContainer.registerKsqlite(
    name: String,
    noinline configure: T.() -> Unit
) = register<T>(name) {
    group = KSQLITE_TASK_GROUP
    configure()
}