package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3TraceEvent
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Callback to use with [ksqlite.capi.sqlite3_trace_v2].
 */
public fun interface Sqlite3TraceCallback<ClientData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/trace_v2.html).
     */
    public fun handle(
        clientData: ClientData,
        event: Sqlite3TraceEvent
    ): Int
}