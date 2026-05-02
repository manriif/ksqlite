package ksqlite.capi.handlers

import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.types.Sqlite3TraceCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.utils.getStringUtf8
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
        pointer1: MemorySegment,
        pointer2: MemorySegment
    ): Int = handler(refPointer) { callback: Sqlite3TraceCallback, userData ->
        dispatchTraceEvent(
            callback = callback,
            userData = userData,
            code = code,
            pointer1 = pointer1,
            pointer2 = pointer2,
            toDb = ::sqlite3,
            toStatement = ::sqlite3_stmt,
            toString = { it.getStringUtf8() },
            toLong = { it.get(ValueLayout.JAVA_LONG, 0) }
        )
    }
}