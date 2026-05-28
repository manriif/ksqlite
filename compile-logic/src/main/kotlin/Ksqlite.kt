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
// Naming
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a new list with all items prefixed with [SQLITE3] and [joint].
 */
fun Iterable<String>.ksqlitePrefixed(joint: Char = '_'): List<String> {
    return map { "${KSQLITE}${joint}${it}" }
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * List of Ksqlite functions extending SQLite ones
 */
val KsqliteFunctions = listOf(
    "auto_extension",
    "cancel_auto_extension",
    "prepare_v2",
    "prepare_v3",
).ksqlitePrefixed()

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