package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.sqlite3_busy_handler].
 */
public fun interface BusyHandlerCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(n: Int): Int
}