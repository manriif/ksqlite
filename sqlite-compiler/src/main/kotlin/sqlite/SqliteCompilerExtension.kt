package sqlite

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Extension for [SqliteCompilerPlugin].
 */
abstract class SqliteCompilerExtension @Inject constructor() {

    /**
     * Version of SQLite, prefixed with the year and posfixed with the checksum.
     */
    abstract val sqliteRelease: Property<SqliteRelease>
    abstract val sqliteDefinitionDirectory: DirectoryProperty
    abstract val sqliteDownloadDirectory: DirectoryProperty
    abstract val sqliteSourcesDirectory: DirectoryProperty
    abstract val sqliteArtefactsDirectory: DirectoryProperty
    abstract val sqliteArtefactsNativeDirectory: DirectoryProperty
}