import compilation.SqliteCompilationParameters
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import toolchains.ToolchainVersions

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
    val sqliteCompilationParameters: Property<SqliteCompilationParameters>

    /**
     * Directory where the SQLite source tree is downloaded and unzipped.
     */
    val sqliteDownloadDirectory: DirectoryProperty

    /**
     * Directory where the SQLite source tree is stored.
     */
    val sqliteSourcesDirectory: DirectoryProperty

    /**
     * Versions of the toolchains to download.
     */
    val toolchainVersions: Property<ToolchainVersions>

    /**
     * Directory where toolchains are located.
     */
    val toolchainsDirectory: DirectoryProperty
}