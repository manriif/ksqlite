package ksqlite.capi.handlers

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.types.Sqlite3BusyHandlerCallback

/**
 * Static C function for [busyHandlerHandler].
 */
internal val BusyHandlerHandler = staticCFunction(::busyHandlerHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
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