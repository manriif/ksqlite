package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback

/**
 * Static C function for [autoVacuumPagesHandler].
 */
internal val AutoVacuumPagesHandler = staticCFunction(::autoVacuumPagesHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
private fun autoVacuumPagesHandler(
    refPointer: COpaquePointer?,
    zSchema: CPointer<ByteVar>?,
    nDbPage: UInt,
    nFreePage: UInt,
    nBytePerPage: UInt
) = handler(refPointer) { callback: Sqlite3AutoVacuumPagesCallback<Any?>, appData ->
    callback.handle(
        appData = appData,
        schemaName = zSchema!!.toKStringFromUtf8(),
        dbPage = nDbPage,
        freePage = nFreePage,
        bytePerPage = nBytePerPage
    )
}