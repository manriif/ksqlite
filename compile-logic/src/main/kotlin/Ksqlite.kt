import komple.gradle.task.configureWithContext
import komple.task.TaskStateTracker
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

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
 * List of typedefs exposed by `ksqlite.h`.
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

const val KSQLITE_TASK_GROUP = "ksqlite"

/**
 * Registers a Ksqlite task.
 */
inline fun <reified T : Task> TaskContainer.registerKsqlite(
    name: String,
    noinline action: T.() -> Unit
) = register<T>(name) {
    group = KSQLITE_TASK_GROUP
    action()
}

/**
 * Registers a Ksqlite task.
 */
inline fun <reified T : Task> TaskContainer.registerKsqliteTracked(
    name: String,
    noinline action: T.(tracker: TaskStateTracker) -> Unit
) = register<T>(name) {
    group = KSQLITE_TASK_GROUP
    configureWithContext(action)
}