package ksqlite.kapi.callbacks

/**
 * Analog to [ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback].
 */
public fun interface AutoExtension {

    /**
     * Initializes the opening database connection.
     * See [ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback].
     */
    public fun ConnectionInitializer.apply()
}