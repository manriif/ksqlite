package ksqlite.foreign.callbacks

/**
 * Callback for use with [ksqlite.foreign.sqlite3_create_collation_v2].
 */
public fun interface CollationCompareCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        lhs: ByteArray,
        rhs: ByteArray
    ): Int
}