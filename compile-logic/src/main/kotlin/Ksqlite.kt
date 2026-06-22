import komple.gradle.task.configureWithContext
import komple.task.TaskStateTracker
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithAction
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction
import org.gradle.kotlin.dsl.registering

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
// Typedefs
///////////////////////////////////////////////////////////////////////////

/**
 * List of typedefs exposed in ksqlite.h
 */
val KsqliteTypedefs = listOf(
    "xLog",
    "xSqllog",
    "xEntryPoint"
).ksqlitePrefixed()

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

fun Task.configureKsqliteTask(cacheable: Boolean) {
    group = KSQLITE_TASK_GROUP

    if (cacheable) {
        outputs.upToDateWhen { true }
    }
}

/**
 * Property delegate for registering new elements in the container.
 */
fun TaskContainer.registeringKsqlite(
    cacheable: Boolean = false,
    action: Task.() -> Unit
): RegisteringDomainObjectDelegateProviderWithAction<out TaskContainer, Task> {
    return registering {
        configureKsqliteTask(cacheable)
        action()
    }
}

/**
 * Property delegate for registering new elements in the container.
 */
inline fun <reified T : Task> TaskContainer.registeringKsqlite(
    cacheable: Boolean = false,
    noinline action: T.() -> Unit
): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, T> {
    return registering(T::class) {
        configureKsqliteTask(cacheable)
        action()
    }
}

/**
 * Property delegate for registering new elements in the container.
 */
inline fun <reified T : Task> TaskContainer.registeringKsqliteTracked(
    cacheable: Boolean = false,
    noinline action: T.(tracker: TaskStateTracker) -> Unit
): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, T> {
    return registeringKsqlite<T>(cacheable) {
        configureWithContext(action)
    }
}