package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.convertActionCode
import ksqlite.capi.callbacks.Sqlite3SetAuthorizerCallback

/**
 * Static C function for [setAuthorizerHandler].
 */
internal val SetAuthorizerHandler = staticCFunction(::setAuthorizerHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
private fun setAuthorizerHandler(
    refPointer: COpaquePointer?,
    action: Int,
    param3: CPointer<ByteVar>?,
    param4: CPointer<ByteVar>?,
    param5: CPointer<ByteVar>?,
    param6: CPointer<ByteVar>?
) = handler(refPointer) { callback: Sqlite3SetAuthorizerCallback, userData ->
    callback(
        userData,
        convertActionCode(action),
        param3?.toKStringFromUtf8(),
        param4?.toKStringFromUtf8(),
        param5?.toKStringFromUtf8(),
        param6?.toKStringFromUtf8()
    ).code
}