package ksqlite

/**
 * Callback for use with [sqlite3_collation_needed].
 */
public fun interface CollationNeededCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        db: Long,
        eTextRep: Int,
        name: String
    )
}