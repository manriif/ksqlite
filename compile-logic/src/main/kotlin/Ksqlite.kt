import komple.gradle.task.configureWithContext
import komple.task.TaskContext
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithAction
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction
import org.gradle.kotlin.dsl.registering
import kotlin.reflect.KClass

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

/**
 * Name of the Kotlin SQLite library
 * It is the name of the library and ksqlite function prefix.
 */
const val KSQLITE = "ksqlite"

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * List of Ksqlite functions extending SQLite ones
 */
val KsqliteFunctions = listOf(
    "auto_extension",
    "cancel_auto_extension"
).map { name ->
    "${KSQLITE}_$name"
}

///////////////////////////////////////////////////////////////////////////
// Tasks
///////////////////////////////////////////////////////////////////////////

private const val KSQLITE_TASK_GROUP = "ksqlite"

private fun Task.configureKsqliteTask(cacheable: Boolean) {
    group = KSQLITE_TASK_GROUP

    if (cacheable) {
        outputs.upToDateWhen { true }
    }
}

/**
 * Property delegate for registering new elements in the container.
 */
fun TaskContainer.registeringKsqlite(
    cacheable: Boolean = true,
    action: Task.(context: TaskContext) -> Unit
): RegisteringDomainObjectDelegateProviderWithAction<out TaskContainer, Task> {
    return registering {
        configureKsqliteTask(cacheable)
        configureWithContext(action)
    }
}

/**
 * Property delegate for registering new elements in the container.
 */
fun <T : Task> TaskContainer.registeringKsqlite(
    type: KClass<T>,
    cacheable: Boolean = true
): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, T> {
    return registering(type) {
        configureKsqliteTask(cacheable)
    }
}

/**
 * Property delegate for registering new elements in the container.
 */
fun <T : Task> TaskContainer.registeringKsqlite(
    type: KClass<T>,
    cacheable: Boolean = true,
    action: T.(context: TaskContext) -> Unit
): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, T> {
    return registering(type) {
        configureKsqliteTask(cacheable)
        configureWithContext(action)
    }
}