import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import utils.cHeaderFile

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

/**
 * Returns the sqlite header file (.h).
 */
fun KsqliteExtension.ksqliteHeaderFile(): Provider<RegularFile> {
    return ksqliteDirectory.map { it.file("$KSQLITE.h") }
}
/*
/**
 * Returns the sqlite source files (.c).
 */
fun Project.ksqliteSourceFiles(extension: KsqliteExtension): ConfigurableFileCollection {
    return objects.fileCollection().from(
        extension.ksqliteDirectory.map { it.file("ksqlite.c") },
        extension.sqliteDirectory.zip(extension.sqliteComponents) { directory, params ->
            directory.file("${params.sqliteMcAmalgamationName}.c")
        }
    )
}*/

///////////////////////////////////////////////////////////////////////////
// Tasks
///////////////////////////////////////////////////////////////////////////

fun Task.createKsqliteTask() {

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