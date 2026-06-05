package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.sqlite3_progress_handler].
 */
public fun interface ProgressHandlerCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(): Int
}