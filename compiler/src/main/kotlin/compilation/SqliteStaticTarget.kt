package compilation

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * Target for static sqlite compilation.
 */
abstract class SqliteStaticTarget {

    @get:Input
    abstract val konanTarget: Property<KonanTarget>

    @get:OutputDirectory
    abstract val libraryDirectory: DirectoryProperty
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the generated library file (.a).
 */
fun SqliteStaticTarget.libraryFile(
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return libraryDirectory.zip(params) { directory, params ->
        val family = konanTarget.get().family
        directory.file("${family.staticPrefix}${params.sqliteName}.${family.staticSuffix}")
    }
}

/**
 * Returns the generated object file (.o).
 */
fun SqliteStaticTarget.objectFile(
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return libraryDirectory.zip(params) { directory, params ->
        directory.file("${params.sqliteName}.o")
    }
}