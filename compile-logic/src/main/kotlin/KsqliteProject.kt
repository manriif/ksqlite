import compilation.SqliteCompilationParameters
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName
import tasks.TASK_EMSCRIPTEN_INSTALL
import tasks.TASK_GNU_SED_INSTALL
import tasks.TASK_JEXTRACT_EXTRACT
import tasks.TASK_SQLITE_INSTALL
import tasks.TASK_TOOLCHAIN_ANDROID_INSTALL
import tasks.TASK_WABT_INSTALL
import tools.Tool
import tools.Toolchains
import java.io.File

/**
 * Name of the ksqlite extension.
 */
const val KSQLITE_EXTENSION_NAME = "ksqlite"

/**
 * Retrieves and returns the [KsqliteExtension] from root project.
 */
val Project.ksqliteExtension: KsqliteExtension
    get() = rootProject.extensions.getByName<KsqliteExtension>(
        KSQLITE_EXTENSION_NAME
    )

///////////////////////////////////////////////////////////////////////////
// Tasks
///////////////////////////////////////////////////////////////////////////

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
 * Returns the provider of the task responsible for installing jextract.
 */
val Project.jextractInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_JEXTRACT_EXTRACT)

/**
 * Returns the provider of the task responsible for installing sqlite.
 */
val Project.sqliteInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_SQLITE_INSTALL)

/**
 * Returns the provider of the task responsible for installing GNU sed.
 */
val Project.gnuSedInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_GNU_SED_INSTALL)

///////////////////////////////////////////////////////////////////////////
// Tool
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a provider to the [tool]'s directory.
 */
fun ProjectLayout.toolDirectory(tool: Provider<Tool>): Provider<Directory> {
    return dir(tool.map { File(it.path) })
}

/**
 * Returns a provider to the [Toolchains] instance.
 */
fun KsqliteExtension.toolchains(): Provider<Toolchains> {
    return compilationParams.map { it.toolchains }
}

/**
 * Returns a provider to the Android [Tool].
 */
fun KsqliteExtension.androidToolchain(): Provider<Tool> {
    return toolchains().map { it.android }
}

///////////////////////////////////////////////////////////////////////////
// SQLite
///////////////////////////////////////////////////////////////////////////

/**
 * Configures ksqlite sources directory.
 */
fun configureKsqliteSources(extension: KsqliteExtension) {
    val generatedHeaderFile = extension.ksqliteDirectory.file("ksqlite-generated.h").get().asFile

    if (!generatedHeaderFile.exists()) {
        val amalgamationHeaderFile = extension.sqliteSourcesDirectory
            .zip(extension.compilationParams) { directory, params ->
                directory.file("${params.sqliteMcAmalgamationName}.h")
            }
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
    return ksqliteDirectory.map { it.file("ksqlite.h") }
}

/**
 * Returns the directories to search for the headers.
 */
fun KsqliteExtension.ksqliteIncludeDirectories(): ConfigurableFileCollection {

}

/**
 * Returns the sqlite source files (.c).
 */
fun KsqliteExtension.ksqliteSourceFiles(): ConfigurableFileCollection {
    listOf(
        ksqliteDirectory.map { it.file("ksqlite.c") },
        sqliteSourcesDirectory.zip(compilationParams) { directory, params ->
            directory.file("${params.sqliteMcAmalgamationName}.c")
        }
    )
}