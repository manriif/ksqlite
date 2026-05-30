package ksqlite

/**
 * Callback for use with [sqlite3_wal_hook].
 */
public fun interface WalHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        db: Long,
        dbName: String,
        nPage: Int
    ): Int
}