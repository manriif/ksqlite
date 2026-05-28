package ksqlite

/**
 * Callback for use with [sqlite3_autovacuum_pages].
 */
public fun interface AutoVacuumPagesCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        zSchema: String,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int
}