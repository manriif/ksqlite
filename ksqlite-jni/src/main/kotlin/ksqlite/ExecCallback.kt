package ksqlite

/**
 * Callback for use with [sqlite3_exec].
 */
public fun interface ExecCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        columnCount: Int,
        columnValues: Array<String?>,
        columnNames: Array<String>
    ): Int
}