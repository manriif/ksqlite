package ksqlite.kapi.callbacks

/**
 * Callback to use with [ksqlite.kapi.connection.Connection.setAutovacuumPages].
 */
public fun interface AutovacuumPages : AutoCloseable {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/autovacuum_pages.html).
     */
    public fun apply(
        schemaName: String,
        dbPage: UInt,
        freePage: UInt,
        bytePerPage: UInt
    ): UInt

    /**
     * Called when the callback is no longer needed by SQLite.
     */
    override fun close(): Unit = Unit
}