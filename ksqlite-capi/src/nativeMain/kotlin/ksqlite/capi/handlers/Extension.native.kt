package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import ksqlite.capi.types.s3
import ksqlite.capi.types.s3_api
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines
import ksqlite.sqlite3_malloc as native_sqlite3_malloc

/**
 * Static C function for [autoExtensionHandler].
 */
internal val SharedExtensionHandler = staticCFunction(::autoExtensionHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_auto_extension].
 * Dispatches sqlite3_auto_extension call to all registered extensions.
 */
private fun autoExtensionHandler(
    db: CPointer<s3>?,
    pzErrMsg: CPointer<CPointerVar<ByteVar>>?,
    pApi: CPointer<s3_api>?
) = autoExtensionHandle(
    db = sqlite3(db!!),
    api = sqlite3_api_routines(pApi!!),
    errorPointer = pzErrMsg
) { errorPointer, message ->
    val cString = message.cstr

    native_sqlite3_malloc(cString.size)?.reinterpret<ByteVar>()?.let { pointer ->
        cString.place(pointer)
        errorPointer.pointed.value = pointer
    }
}