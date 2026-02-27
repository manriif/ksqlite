@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.handlers

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.types.Sqlite3ProgressHandlerCallback

/**
 * Static C function for [progressHandlerHandler].
 */
internal val ProgressHandlerHandler = staticCFunction(::progressHandlerHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
private fun progressHandlerHandler(
    refPointer: COpaquePointer?
) = handler(refPointer) { callback: Sqlite3ProgressHandlerCallback, userData ->
    callback(userData)
}