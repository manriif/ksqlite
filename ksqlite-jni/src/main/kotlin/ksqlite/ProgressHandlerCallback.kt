package ksqlite

/**
 * Callback for use with [sqlite3_progress_handler].
 */
public fun interface ProgressHandlerCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(): Int
}