import compilation.SqliteCompilationParameters
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Extension for [KsqliteCompilerPlugin].
 */
interface KsqliteCompilerExtension {

    /**
     * Checksums of downloaded sources.
     */
    val checksums: Property<KsqliteChecksums>

    /**
     * Parameters for the SQLite compilation.
     */
    val compilationParams: Property<SqliteCompilationParameters>

    /**
     * Directory where to put downloaded files.
     */
    val downloadDirectory: DirectoryProperty

    /**
     * Directory where the SQLite source tree is stored.
     */
    val sqliteSourcesDirectory: DirectoryProperty

    /**
     * Version of the JDK.
     */
    val jdkVersion: Property<String>

    /**
     * Version of JExtract.
     */
    val jExtractVersion: Property<String>

    /**
     * Directory where eJextract is located.
     */
    val jExtractDirectory: DirectoryProperty
}