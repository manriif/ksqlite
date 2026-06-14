package ksqlite.foreign.callbacks

/**
 * Callback for use with [ksqlite.foreign.sqlite3_exec].
 */
public fun interface ExecCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        columnCount: Int,
        columnValues: Array<String?>,
        columnNames: Array<String>
    ): Int
}