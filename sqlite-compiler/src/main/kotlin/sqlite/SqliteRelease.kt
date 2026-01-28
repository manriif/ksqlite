package sqlite

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/**
 * Sqlite release components.
 */
data class SqliteRelease(
    /**
     * Release year.
     */
    @get:Input
    val year: String,

    /**
     * Version major.
     */
    @get:Input
    val major: String,

    /**
     * Version minor.
     */
    @get:Input
    val minor: String,

    /**
     * Version patch.
     */
    @get:Input
    val patch: String,

    /**
     * Version build.
     */
    @get:Input
    val build: String,

    /**
     * Checksum of the amalgamation zip.
     */
    @get:Input
    val checksum: String
) {

    /**
     * Name of the SQLite product.
     * It is the name of the C header file, C source file and code source function prefix.
     */
    @get:Internal
    val sqliteName: String = "sqlite$major"

    /**
     * Normalized SQLite version name.
     */
    @get:Internal
    val normalizedVersion: String
        get() = buildNormalizedVersion()

    /**
     * Returns the normalized SQLite version name.
     */
    private fun buildNormalizedVersion(): String = buildString {
        append(major)
        append(minor.padStart(2, '0'))
        append(patch.padStart(2, '0'))
        append(build.padStart(2, '0'))
    }
}