import compilation.SqliteCompilationParameters
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByName
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
 * Returns the name of the SQLite dynamic library.
 */
fun sqliteDynamicLibraryName(params: Provider<SqliteCompilationParameters>): Provider<String> {
    return params.map { it.sqliteName }
}

/**
 * Returns the sqlite header file (.h).
 */
fun sqliteHeaderFile(
    sources: Provider<Directory>,
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return sources.zip(params) { directory, params ->
        directory.file("${params.sqliteMcName}.h")
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
        directory.file("${params.sqliteMcName}.c")
    }
}