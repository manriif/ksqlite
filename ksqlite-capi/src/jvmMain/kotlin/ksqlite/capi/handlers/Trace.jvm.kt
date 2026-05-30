package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3TraceCallback
import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_stmt
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
internal class TraceHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
    )

    fun handle(
        code: Int,
        refPointer: MemorySegment,
        pPointer: MemorySegment,
        xPointer: MemorySegment
    ): Int = handler(refPointer) { callback: Sqlite3TraceCallback<Any?>, appData ->
        dispatchTraceEvent(
            callback = callback,
            appData = appData,
            code = code,
            pPointer = pPointer,
            xPointer = xPointer,
            toDb = ::sqlite3,
            toStatement = ::sqlite3_stmt,
            toString = { it.toKStringFromUtf8() },
            toLong = { it.get(ValueLayout.JAVA_LONG, 0) }
        )
    }
}