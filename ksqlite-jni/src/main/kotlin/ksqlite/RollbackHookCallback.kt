package ksqlite

/**
 * Callback for [sqlite3_rollback_hook].
 */
public fun interface RollbackHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun call()
}