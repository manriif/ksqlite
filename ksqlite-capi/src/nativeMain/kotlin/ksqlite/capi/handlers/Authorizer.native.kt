package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.types.internal.convertActionCode

/**
 * Static C function for [AuthorizerHandler].
 */
internal val AuthorizerHandler = staticCFunction(::authorizerHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
private fun authorizerHandler(
    refPointer: COpaquePointer?,
    action: Int,
    param3: CPointer<ByteVar>?,
    param4: CPointer<ByteVar>?,
    param5: CPointer<ByteVar>?,
    param6: CPointer<ByteVar>?
) = handle(refPointer) { callback: SqliteAuthorizerCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        action = convertActionCode(action),
        detail1 = param3?.toKStringFromUtf8(),
        detail2 = param4?.toKStringFromUtf8(),
        detail3 = param5?.toKStringFromUtf8(),
        detail4 = param6?.toKStringFromUtf8()
    ).code
}