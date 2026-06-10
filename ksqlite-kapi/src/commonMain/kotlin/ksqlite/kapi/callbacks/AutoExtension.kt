package ksqlite.kapi.callbacks

import ksqlite.kapi.SQLiteConnectionInitializer

/**
 * Analog to [ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback].
 */
public fun interface AutoExtension {

    /**
     * Initializes the opening database connection.
     * See [ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback].
     */
    public fun SQLiteConnectionInitializer.apply()
}