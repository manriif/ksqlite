import compilation.SqliteCompilationParameters
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Extension for [SqliteCompilerPlugin].
 */
interface SqliteCompilerExtension {

    /**
     * Parameters for the SQLite compilation.
     */
    val sqliteCompilationParameters: Property<SqliteCompilationParameters>

    /**
     * SHA-256 Checksum of the SQLite downloaded sources.
     */
    val sqliteDownloadChecksum: Property<String>

    /**
     * Directory where the SQLite source tree is downloaded and unzipped.
     */
    val sqliteDownloadDirectory: DirectoryProperty

    /**
     * Directory where the SQLite source tree is stored.
     */
    val sqliteSourcesDirectory: DirectoryProperty
}