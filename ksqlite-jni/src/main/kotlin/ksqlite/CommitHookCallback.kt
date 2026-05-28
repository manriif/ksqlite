package ksqlite

/**
 * Callback for use with [sqlite3_commit_hook].
 */
public fun interface CommitHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(): Int
}