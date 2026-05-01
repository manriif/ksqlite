import komple.gradle.task.configureWithContext
import komple.task.TaskContext
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithAction
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction
import org.gradle.kotlin.dsl.registering
import utils.cHeaderFile
import kotlin.reflect.KClass

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

/**
 * Name of the Kotlin SQLite library
 * It is the name of the library and ksqlite function prefix.
 */
const val KSQLITE = "ksqlite"

/**
 * Name of the generated header file.
 */
const val GENERATED_HEADER_FILE_NAME = "ksqlite-generated"

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * List of Ksqlite functions extending SQLite ones
 */
val KsqliteFunctions = listOf(
    "auto_extension"
).map { name ->
    "${KSQLITE}_$$name"
}

///////////////////////////////////////////////////////////////////////////
// Configuration
///////////////////////////////////////////////////////////////////////////

/**
 * Configures the ksqlite directory.
 */
fun configureKsqlite(extension: KsqliteExtension) {
    val generatedHeaderFileName = cHeaderFile(GENERATED_HEADER_FILE_NAME)
    val generatedHeaderFile = extension.ksqliteDirectory.file(generatedHeaderFileName).get().asFile

    if (!generatedHeaderFile.exists()) {
        val amalgamationHeaderFile = extension.sqliteDirectory
            .map { it.file(cHeaderFile(SQLITE3_MC_AMALGAMATION)) }
            .get()
            .asFile

        generatedHeaderFile.writeText(
            """
                |#include "${amalgamationHeaderFile.absolutePath}"
            """.trimMargin()
        )
    }
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