package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_stmt
import ksqlite.foreign.callbacks.TraceCallback

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
internal class TraceHandler<AppData> :
    Handler<SqliteTraceCallback<AppData>, AppData>(),
    TraceCallback {

    override fun apply(
        code: Int,
        pPointer: Long,
        xPointer: Any?
    ): Int = handle { callback, appData ->
        dispatchTraceEvent(
            callback = callback,
            appData = appData,
            code = code,
            pPointer = pPointer,
            xPointer = xPointer,
            toDb = ::sqlite3,
            toStatement = ::sqlite3_stmt,
            toString = { it as String },
            toLong = { it as Long }
        )
    }
}