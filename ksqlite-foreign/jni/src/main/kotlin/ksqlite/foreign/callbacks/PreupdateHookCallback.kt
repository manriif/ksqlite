package ksqlite.foreign.callbacks

/**
 * Callback for use with [ksqlite.foreign.sqlite3_preupdate_hook].
 */
public fun interface PreupdateHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        db: Long,
        op: Int,
        dbName: String,
        dbTable: String,
        iKey1: Long,
        iKey2: Long
    )
}