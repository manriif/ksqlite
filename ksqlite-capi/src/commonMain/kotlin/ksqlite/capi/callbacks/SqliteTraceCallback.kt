package ksqlite.capi.callbacks

import ksqlite.capi.types.SqliteTraceEvent

/**
 * Callback to use with [ksqlite.capi.sqlite3_trace_v2].
 */
public fun interface SqliteTraceCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/trace_v2.html).
     */
    public fun apply(
        appData: AppData,
        event: SqliteTraceEvent
    ): Int
}