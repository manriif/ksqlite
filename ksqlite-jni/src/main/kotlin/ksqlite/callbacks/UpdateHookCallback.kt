package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.sqlite3_update_hook].
 */
public fun interface UpdateHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        opId: Int,
        dbName: String,
        tableName: String,
        rowId: Long
    )
}