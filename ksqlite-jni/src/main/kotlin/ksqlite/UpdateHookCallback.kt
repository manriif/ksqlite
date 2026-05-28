package ksqlite

/**
 * Callback for use with [sqlite3_update_hook].
 */
public fun interface UpdateHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        opId: Int,
        dbName: String,
        tableName: String,
        rowId: Long
    )
}