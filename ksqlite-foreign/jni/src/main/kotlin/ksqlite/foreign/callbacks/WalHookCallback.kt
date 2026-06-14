package ksqlite.foreign.callbacks

/**
 * Callback for use with [ksqlite.foreign.sqlite3_wal_hook].
 */
public fun interface WalHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        db: Long,
        dbName: String,
        nPage: Int
    ): Int
}