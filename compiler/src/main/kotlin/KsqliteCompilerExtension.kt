import compilation.SqliteCompilationParameters
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Extension for [KsqliteCompilerPlugin].
 */
interface KsqliteCompilerExtension {

    /**
     * Checksums of downloaded sources.
     */
    val checksums: Property<KsqliteChecksums>

    /**
     * Directory where to put downloaded files.
     */
    val downloadDirectory: DirectoryProperty

    /**
     * Directory where toolchains are located.
     */
    val toolchainsDirectory: DirectoryProperty

    /**
     * Directory where the SQLite source tree is stored.
     */
    val sqliteSourcesDirectory: DirectoryProperty

    /**
     * Parameters for the SQLite compilation.
     */
    val compilationParams: Property<SqliteCompilationParameters>
}