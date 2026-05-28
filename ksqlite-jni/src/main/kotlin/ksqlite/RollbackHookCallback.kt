package ksqlite

/**
 * Callback for use with [sqlite3_rollback_hook].
 */
public fun interface RollbackHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun call()
}