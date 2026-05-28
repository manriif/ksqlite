package ksqlite

/**
 * Callback for use with [sqlite3_preupdate_hook].
 */
public fun interface PreupdateHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        db: Long,
        op: Int,
        dbName: String,
        dbTable: String,
        iKey1: Long,
        iKey2: Long
    )
}