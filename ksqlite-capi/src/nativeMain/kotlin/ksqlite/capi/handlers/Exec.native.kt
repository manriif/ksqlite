package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.Sqlite3ExecCallback
import ksqlite.capi.memory.toArray

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
) = handler(refPointer) { callback: Sqlite3ExecCallback, userData ->
    val columnValues = values?.toArray(columnCount) { it?.toKStringFromUtf8() } ?: emptyArray()
    val columnNames = names?.toArray(columnCount) { it!!.toKStringFromUtf8() } ?: emptyArray()

    callback(
        userData,
        columnCount,
        columnValues,
        columnNames
    )
}