package ksqlite.capi.handlers

import ksqlite.capi.types.Sqlite3AutoExtensionCallback

/**
 * All registered [Sqlite3AutoExtensionCallback].
 */
internal val AutoExtensions = mutableListOf<Sqlite3AutoExtensionCallback>()