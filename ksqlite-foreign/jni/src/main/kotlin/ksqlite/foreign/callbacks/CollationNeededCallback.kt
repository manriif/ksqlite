package ksqlite.foreign.callbacks

/**
 * Callback for use with [ksqlite.foreign.sqlite3_collation_needed].
 */
public fun interface CollationNeededCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        db: Long,
        eTextRep: Int,
        name: String
    )
}