import compilation.SqliteCompilationParameters
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName
import tasks.TASK_EMSCRIPTEN_INSTALL
import tasks.TASK_JEXTRACT_EXTRACT
import tasks.TASK_SQLITE_INSTALL
import tasks.TASK_TOOLCHAIN_ANDROID_EXTRACT
import toolchains.Toolchain
import toolchains.Toolchains
import java.io.File

/**
 * Name of the sqlite compiler extension.
 */
const val KSQLITE_COMPILER_EXTENSION_NAME = "ksqliteCompiler"

/**
 * Retrieves and returns the [KsqliteCompilerExtension] from root project.
 */
val Project.ksqliteCompilerExtension: KsqliteCompilerExtension
    get() = rootProject.extensions.getByName<KsqliteCompilerExtension>(
        KSQLITE_COMPILER_EXTENSION_NAME
    )

///////////////////////////////////////////////////////////////////////////
// Tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the provider of the task responsible for installing the Android NDK toolchain.
 */
val Project.androidToolchainInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_TOOLCHAIN_ANDROID_EXTRACT)

/**
 * Returns the provider of the task responsible for installing emscripten.
 */
val Project.emscriptenInstallTaskProvider: TaskProvider<Task>
    get() = rootProject.tasks.named(TASK_EMSCRIPTEN_INSTALL)

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


///////////////////////////////////////////////////////////////////////////
// Toolchains
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a provider to the [Toolchains] instance.
 */
fun KsqliteCompilerExtension.toolchains(): Provider<Toolchains> {
    return compilationParams.map { it.toolchains }
}

/**
 * Returns a provider to the [toolchain]'s directory.
 */
fun ProjectLayout.toolchainDirectory(toolchain: Provider<Toolchain>): Provider<Directory> {
    return dir(toolchain.map { File(it.path) })
}

/**
 * Returns a provider to the Android [Toolchain].
 */
fun KsqliteCompilerExtension.androidToolchain(): Provider<Toolchain> {
    return toolchains().map { it.android }
}

///////////////////////////////////////////////////////////////////////////
// SQLite
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the sqlite header file (.h).
 */
fun sqliteHeaderFile(
    sources: Provider<Directory>,
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return sources.zip(params) { directory, params ->
        directory.file("${params.sqliteMcAmalgamationName}.h")
    }
}

/**
 * Returns the sqlite source file (.c).
 */
fun sqliteSourceFile(
    sources: Provider<Directory>,
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return sources.zip(params) { directory, params ->
        directory.file("${params.sqliteMcAmalgamationName}.c")
    }
}