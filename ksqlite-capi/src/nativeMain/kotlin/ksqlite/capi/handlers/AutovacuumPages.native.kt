package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback

/**
 * Static C function for [autovacuumPagesHandler].
 */
internal val AutovacuumPagesHandler = staticCFunction(::autovacuumPagesHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
private fun autovacuumPagesHandler(
    refPointer: COpaquePointer?,
    zSchema: CPointer<ByteVar>?,
    nDbPage: UInt,
    nFreePage: UInt,
    nBytePerPage: UInt
) = handle(refPointer) { callback: SqliteAutovacuumPagesCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        schemaName = zSchema!!.toKStringFromUtf8(),
        dbPage = nDbPage,
        freePage = nFreePage,
        bytePerPage = nBytePerPage
    )
}