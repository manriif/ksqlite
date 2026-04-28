package tools.sqlitemc

import komple.tool.extension.HasChecksumSupport
import komple.tool.extension.HasVersionSupport
import komple.tool.extension.KompleToolExtension

/**
 * Extension for Sqlite Multiple Ciphers.
 */
interface SqliteMCExtension :
    KompleToolExtension,
    HasVersionSupport,
    HasChecksumSupport