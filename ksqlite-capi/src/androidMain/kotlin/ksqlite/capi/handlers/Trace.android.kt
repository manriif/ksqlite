package ksqlite.capi.handlers

import ksqlite.TraceCallback
import ksqlite.capi.callbacks.Sqlite3TraceCallback
import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_stmt

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
internal class TraceHandler<AppData> :
    Handler<Sqlite3TraceCallback<AppData>, AppData>(),
    TraceCallback {

    override fun call(
        code: Int,
        pPointer: Long,
        xPointer: Any?
    ): Int = handler { callback, appData ->
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