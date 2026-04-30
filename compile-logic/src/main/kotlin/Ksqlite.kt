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
    action: T.() -> Unit
): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, T> {
    return registering(type) {
        configureKsqliteTask(cacheable)
        action()
    }
}

/*
/**
 * Returns the provider of the task responsible for installing the Android NDK toolchain.
 */
val Project.androidToolchainInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_TOOLCHAIN_ANDROID_INSTALL)

/**
 * Returns the provider of the task responsible for installing emscripten.
 */
val Project.emscriptenInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_EMSCRIPTEN_INSTALL)

/**
 * Returns the provider of the task responsible for installing wabt.
 */
val Project.wabtInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_WABT_INSTALL)

/**
 * Returns the provider of the task responsible for installing sqlite.
 */
val Project.sqliteInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_SQLITE_INSTALL)

/**
 * Returns the provider of the task responsible for installing GNU sed.
 */
val Project.gnuSedInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_GNU_SED_INSTALL)*/