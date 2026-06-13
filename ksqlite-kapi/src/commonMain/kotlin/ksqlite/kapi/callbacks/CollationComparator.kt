package ksqlite.kapi.callbacks

/**
 * Analog callback to [ksqlite.capi.callbacks.Sqlite3CollationCompareCallback].
 */
public fun interface CollationComparator {

    /**
     * See [ksqlite.capi.callbacks.Sqlite3CollationCompareCallback].
     */
    public fun apply(
        schemaName: String,
        dbPage: UInt,
        freePage: UInt,
        bytePerPage: UInt
    ): UInt

    /**
     * Called when the callback is no longer needed.
     * Provides the opportunity to perform some cleanup.
     */
    public fun destroy(): Unit = Unit
}