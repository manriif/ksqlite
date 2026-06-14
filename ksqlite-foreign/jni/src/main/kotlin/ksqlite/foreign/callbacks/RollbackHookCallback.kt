package ksqlite.foreign.callbacks

/**
 * Callback for use with [ksqlite.foreign.sqlite3_rollback_hook].
 */
public fun interface RollbackHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply()
}