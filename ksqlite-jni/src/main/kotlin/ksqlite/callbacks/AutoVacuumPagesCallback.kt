package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.sqlite3_autovacuum_pages].
 */
public fun interface AutoVacuumPagesCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        zSchema: String,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int
}