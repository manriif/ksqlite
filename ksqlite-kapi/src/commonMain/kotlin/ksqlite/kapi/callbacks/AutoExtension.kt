package ksqlite.kapi.callbacks

/**
 * Analog to [ksqlite.capi.callbacks.SqliteAutoExtensionCallback].
 */
public fun interface AutoExtension {

    /**
     * Initializes the opening database connection.
     * See [ksqlite.capi.callbacks.SqliteAutoExtensionCallback].
     */
    public fun ConnectionInitializer.apply()
}