import compilation.SqliteCompilationParameters
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import tools.Tools

/**
 * Extension for [KsqlitePlugin].
 */
interface KsqliteExtension {

    /**
     * Checksums of downloaded sources.
     */
    val checksums: Property<KsqliteChecksums>

    /**
     * Parameters for the SQLite compilation.
     */
    val compilationParams: Property<SqliteCompilationParameters>

    /**
     * Tools used by tasks.
     */
    val tools: Property<Tools>

    /**
     * Directory where to put downloaded files.
     */
    val downloadDirectory: DirectoryProperty

    /**
     * Directory where the SQLite source tree is stored.
     */
    val sqliteSourcesDirectory: DirectoryProperty

    /**
     * Year SQLite was released.
     */
    val sqliteReleaseYear: Property<String>

    /**
     * Version of the JDK.
     */
    val jdkVersion: Property<String>
}