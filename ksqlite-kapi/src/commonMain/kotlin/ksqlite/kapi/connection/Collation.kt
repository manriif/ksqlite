package ksqlite.kapi.connection

/**
 * Callback to use with [Connection.createCollation].
 */
public fun interface Collation {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/create_collation.html).
     */
    public fun apply(
        lhs: ByteArray,
        rhs: ByteArray
    ): UInt

    /**
     * Called when the callback is no longer needed.
     * Provides the opportunity to perform some cleanup.
     */
    public fun destroy(): Unit = Unit
}