package ksqlite

/**
 * Callback for [sqlite3_create_collation_v2].
 */
public fun interface CollationCompareCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        lhs: ByteArray,
        rhs: ByteArray
    ): Int
}