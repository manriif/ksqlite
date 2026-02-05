package compilation

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import platform.Library
import platform.Platform

/**
 * Target for static sqlite compilation.
 */
abstract class SqliteTarget {

    /**
     * Targeted platform.
     */
    @get:Input
    abstract val platform: Property<Platform>

    /**
     * File to write compiled library to.
     */
    @get:Internal
    abstract val libraryFile: RegularFileProperty
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the name of generated shared library file.
 */
fun Library.sharedLibraryFileName(libraryName: String): String {
    return "${sharedPrefix}${libraryName}.${sharedSuffix}"
}

/**
 * Returns the name of generated static library file.
 */
fun Library.staticLibraryFileName(libraryName: String): String {
    return "${staticPrefix}${libraryName}.${staticSuffix}"
}