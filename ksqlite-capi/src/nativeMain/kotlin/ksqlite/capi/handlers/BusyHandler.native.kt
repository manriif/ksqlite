package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.types.Sqlite3BusyHandlerCallback

/**
 * Static C function for [busyHandlerHandler].
 */
internal val BusyHandlerHandler = staticCFunction(::busyHandlerHandler)

/**
 * Handler for [ksqlite.sqlite3_busy_handler].
 */
private fun busyHandlerHandler(
    refPointer: COpaquePointer?,
    count: Int,
) = handler(refPointer) { callback: Sqlite3BusyHandlerCallback, userData ->
    callback(
        userData,
        count
    )
}