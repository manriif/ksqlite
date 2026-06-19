package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.types.s3
import ksqlite.capi.types.sqlite3

/**
 * Static C function for [walHookHandler].
 */
internal val WalHookHandler = staticCFunction(::walHookHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_wal_hook].
 */
private fun walHookHandler(
    refPointer: COpaquePointer?,
    db: CPointer<s3>?,
    dbName: CPointer<ByteVar>?,
    nPage: Int,
) = handle(refPointer) { callback: SqliteWalHookCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        db = sqlite3(db!!),
        databaseName = dbName!!.toKStringFromUtf8(),
        pageCount = nPage
    ).code
}