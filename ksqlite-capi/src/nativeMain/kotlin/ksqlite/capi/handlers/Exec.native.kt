package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.Sqlite3ExecCallback
import ksqlite.capi.memory.toNullableStringArrayOrEmpty
import ksqlite.capi.memory.toStringArrayOrEmpty

/**
 * Static C function for [execHandler].
 */
internal val ExecHandler = staticCFunction(::execHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
private fun execHandler(
    refPointer: COpaquePointer?,
    columnCount: Int,
    values: CPointer<CPointerVar<ByteVar>>?,
    names: CPointer<CPointerVar<ByteVar>>?
) = handler(refPointer) { callback: Sqlite3ExecCallback<Any?>, appData ->
    callback.handle(
        appData = appData,
        columnCount = columnCount,
        columnValues = values.toNullableStringArrayOrEmpty(columnCount),
        columnNames = names.toStringArrayOrEmpty(columnCount)
    )
}