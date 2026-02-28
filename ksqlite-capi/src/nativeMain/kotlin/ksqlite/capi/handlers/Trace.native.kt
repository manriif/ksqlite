package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value
import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.types.Sqlite3TraceCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_stmt

/**
 * Static C function for [traceHandler].
 */
internal val TraceHandler = staticCFunction(::traceHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
private fun traceHandler(
    code: UInt,
    refPointer: COpaquePointer?,
    pointer1: COpaquePointer?,
    pointer2: COpaquePointer?
) = handler(refPointer) { callback: Sqlite3TraceCallback, userData ->
    dispatchTraceEvent(
        callback = callback,
        userData = userData,
        code = code.toInt(),
        pointer1 = pointer1,
        pointer2 = pointer2,
        toDb = { sqlite3(it.reinterpret()) },
        toStatement = { sqlite3_stmt(it.reinterpret()) },
        toString = { it.reinterpret<ByteVar>().toKStringFromUtf8() },
        toLong = { it.reinterpret<LongVar>().pointed.value }
    )
}