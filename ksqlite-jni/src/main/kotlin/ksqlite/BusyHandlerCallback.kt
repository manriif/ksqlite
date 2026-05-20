package ksqlite

/**
 * Callback for [sqlite3_busy_handler].
 */
public fun interface BusyHandlerCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(n: Int): Int
}