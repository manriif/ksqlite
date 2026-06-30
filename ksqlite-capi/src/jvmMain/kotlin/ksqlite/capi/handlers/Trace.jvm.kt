package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.foreign.`sqlite3_trace_v2$xCallback`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
internal class TraceHandler :
    Handler(),
    `sqlite3_trace_v2$xCallback`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_trace_v2$xCallback`.allocate(this, arena)

    override fun apply(
        code: Int,
        refPointer: MemorySegment,
        pPointer: MemorySegment,
        xPointer: MemorySegment
    ): Int = handle(refPointer) { callback: SqliteTraceCallback<Any?>, appData ->
        dispatchTraceEvent(
            callback = callback,
            appData = appData,
            code = code,
            pPointer = pPointer,
            xPointer = xPointer,
            toDb = ::sqlite3,
            toStatement = ::sqlite3_stmt,
            toString = MemorySegment::toKStringFromUtf8,
            toLong = { it.get(ValueLayout.JAVA_LONG, 0) }
        )
    }
}