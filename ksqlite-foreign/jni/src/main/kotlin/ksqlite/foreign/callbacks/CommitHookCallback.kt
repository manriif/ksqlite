package ksqlite.foreign.callbacks

/**
 * Callback for use with [ksqlite.foreign.sqlite3_commit_hook].
 */
public fun interface CommitHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(): Int
}