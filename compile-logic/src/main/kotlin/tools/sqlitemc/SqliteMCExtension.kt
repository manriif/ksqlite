package tools.sqlitemc

import komple.tool.extension.HasChecksumSupport
import komple.tool.extension.HasVersionSupport
import komple.tool.extension.KompleToolExtension
import org.gradle.api.provider.Property

/**
 * Extension for SQLite Multiple Ciphers.
 */
interface SqliteMCExtension :
    KompleToolExtension,
    HasVersionSupport,
    HasChecksumSupport {

    /**
     * Version of SQLite targeted by the version of SQLite Multiple Ciphers.
     */
    val sqliteVersion: Property<String>
}