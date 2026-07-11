package tools.sqlite

import komple.tool.extension.HasChecksumSupport
import komple.tool.extension.HasVersionSupport
import komple.tool.extension.KompleToolExtension
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Extension for SQLite.
 */
interface SqliteExtension :
    KompleToolExtension,
    HasVersionSupport,
    HasChecksumSupport {

    /**
     * Year the [version] of SQLite was released.
     */
    val releaseYear: Property<String>

    /**
     * Directory where the ksqlite sources are located.
     */
    val ksqliteDirectory: DirectoryProperty

    /**
     * Directory where SQLite Multiple Ciphers is installed.
     */
    val sqliteMcDirectory: DirectoryProperty
}