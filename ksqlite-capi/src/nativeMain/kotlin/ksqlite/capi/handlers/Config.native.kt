package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.capi.types.s3
import ksqlite.capi.types.sqlite3

///////////////////////////////////////////////////////////////////////////
// Log
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [configLogHandler].
 */
internal val ConfigLogHandler = staticCFunction(::configLogHandler)

/**
 * Handler for the LOG option of [ksqlite.capi.sqlite3_config].
 */
private fun configLogHandler(
    refPointer: COpaquePointer?,
    errCode: Int,
    errMsg: CPointer<ByteVar>?
) = handle(refPointer) { callback: SqliteConfigLogCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        errorCode = errCode,
        message = errMsg?.toKStringFromUtf8()
    )
}

///////////////////////////////////////////////////////////////////////////
// SqlLog
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [configLogHandler].
 */
internal val ConfigSqlLogHandler = staticCFunction(::configSqlLogHandler)

/**
 * Handler for the SQLLOG option of [ksqlite.capi.sqlite3_config].
 */
private fun configSqlLogHandler(
    refPointer: COpaquePointer?,
    db: CPointer<s3>?,
    name: CPointer<ByteVar>?,
    type: Int
) = handle(refPointer) { callback: SqliteConfigSqlLogCallback<Any?>, appData ->
    dispatchSqlLogEvent(
        callback = callback,
        appData = appData,
        type = type,
        db = sqlite3(db!!),
        name = name?.toKStringFromUtf8()
    )
}