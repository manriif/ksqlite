import io.github.z4kn4fein.semver.Version
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/**
 * Sqlite release components.
 */
data class SqliteRelease(
    /**
     * Version of SQLite.
     */
    val sqliteVersion: Version,

    /**
     * Version of the SQLite MC library.
     */
    val sqliteMultipleCiphersVersion: Version,

    /**
     * SHA-256 checksum of the amalgamation zip.
     */
    @get:Input
    val sha256checksum: String
) {

    /**
     * Name of the SQLite product.
     * It is the name of the default C header file, C source file and code source function prefix.
     */
    @get:Internal
    val sqliteName: String = "sqlite${sqliteVersion.major}"

    /**
     * Name of the SQLite product.
     * It is the name of the C header file, C source file and code source function prefix.
     */
    @get:Internal
    val sqliteMcName: String = "sqlite${sqliteVersion.major}mc_amalgamation"

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    companion object {

        /**
         * Returns an [SqliteRelease] from [sqliteVersion] and [sqliteMultipleCiphersVersion].
         *
         * The [sqliteMultipleCiphersVersion] must contains the SHA256 checksum of the amalgamation
         * source file as the last version component.
         */
        fun parse(sqliteVersion: String, sqliteMultipleCiphersVersion: String): SqliteRelease {
            val sqliteVersion = Version.parse(sqliteVersion)
            val (major, minor, patch, sha256checksum) = sqliteMultipleCiphersVersion.split('.')
            val sqliteMultipleCiphersVersion = Version(major.toInt(), minor.toInt(), patch.toInt())

            return SqliteRelease(
                sqliteVersion = sqliteVersion,
                sqliteMultipleCiphersVersion = sqliteMultipleCiphersVersion,
                sha256checksum = sha256checksum
            )
        }
    }
}