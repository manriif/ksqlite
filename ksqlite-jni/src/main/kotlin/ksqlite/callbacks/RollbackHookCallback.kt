package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.sqlite3_rollback_hook].
 */
public fun interface RollbackHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply()
}