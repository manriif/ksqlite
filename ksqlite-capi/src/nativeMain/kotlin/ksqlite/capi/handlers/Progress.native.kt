@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.handlers

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.SqliteProgressHandlerCallback

/**
 * Static C function for [progressHandlerHandler].
 */
internal val ProgressHandlerHandler = staticCFunction(::progressHandlerHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
private fun progressHandlerHandler(
    refPointer: COpaquePointer?
) = handle(refPointer) { callback: SqliteProgressHandlerCallback<Any?>, appData ->
    callback.apply(appData)
}