package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.sqlite3_trace_v2].
 */
public fun interface TraceCallback {

    /**
     * Invoked from JNI.
     *
     * - [pPointer] is a [Long] pointing to a `sqlite3` or `sqlite3_stmt` depending on [code].
     * - [xPointer] is a [Long] or a [String] depending on [code].
     */
    public fun apply(
        code: Int,
        pPointer: Long,
        xPointer: Any?
    ): Int
}