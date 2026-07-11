package ksqlite.kapi.database

/**
 * Callback to use with [DatabaseConnection.createCollation].
 */
public fun interface Collation : AutoCloseable {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/create_collation.html).
     */
    public fun apply(
        lhs: ByteArray,
        rhs: ByteArray
    ): Int

    /**
     * Called when the collation is no longer needed by SQLite.
     */
    override fun close(): Unit = Unit
}