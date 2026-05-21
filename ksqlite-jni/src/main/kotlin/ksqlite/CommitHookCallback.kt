package ksqlite

/**
 * Callback for [sqlite3_commit_hook].
 */
public fun interface CommitHookCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(): Int
}