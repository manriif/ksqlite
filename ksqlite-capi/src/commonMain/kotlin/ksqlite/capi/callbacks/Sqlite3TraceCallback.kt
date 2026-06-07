package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3TraceEvent

/**
 * Callback to use with [ksqlite.capi.sqlite3_trace_v2].
 */
public fun interface Sqlite3TraceCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/trace_v2.html).
     */
    public fun apply(
        appData: AppData,
        event: Sqlite3TraceEvent
    ): Int
}