package compilation

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import platform.Platform

/**
 * Target for static sqlite compilation.
 */
abstract class SqliteTarget {

    @get:Input
    abstract val platform: Property<Platform>

    @get:OutputDirectory
    abstract val libraryDirectory: DirectoryProperty
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the generated shared library file.
 */
fun SqliteTarget.sharedLibraryFile(
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return libraryDirectory.zip(params) { directory, params ->
        platform.get().operatingSystem.library.run {
            directory.file("${sharedPrefix}${params.sqliteName}.${sharedSuffix}")
        }
    }
}

/**
 * Returns the generated static library file.
 */
fun SqliteTarget.staticLibraryFile(
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return libraryDirectory.zip(params) { directory, params ->
        platform.get().operatingSystem.library.run {
            directory.file("${staticPrefix}${params.sqliteName}.${staticSuffix}")
        }
    }
}

/**
 * Returns the generated object file (.o).
 */
fun SqliteTarget.objectFile(
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return libraryDirectory.zip(params) { directory, params ->
        directory.file("${params.sqliteName}.o")
    }
}