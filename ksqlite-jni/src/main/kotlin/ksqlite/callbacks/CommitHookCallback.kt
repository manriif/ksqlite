package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.sqlite3_commit_hook].
 */
public fun interface CommitHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(): Int
}