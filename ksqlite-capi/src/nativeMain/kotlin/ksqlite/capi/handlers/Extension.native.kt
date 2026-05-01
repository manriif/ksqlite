package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import ksqlite.SQLITE_OK
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.s3
import ksqlite.capi.types.s3_api
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines
import ksqlite.sqlite3_malloc as native_sqlite3_malloc

/**
 * Static C function for [autoExtensionHandler].
 */
internal val AutoExtensionHandler = staticCFunction(::autoExtensionHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_auto_extension].
 * Dispatches sqlite3_auto_extension call to all registered extensions.
 */
private fun autoExtensionHandler(
    db: CPointer<s3>?,
    pzErrMsg: CPointer<CPointerVar<ByteVar>>?,
    pApi: CPointer<s3_api>?
): Int {
    var result = SQLITE_OK
    var errorMessage: String? = null
    val db = sqlite3(db!!)
    val api = sqlite3_api_routines(pApi!!)
    val iterator = AutoExtensions.iterator()

    while (iterator.hasNext() && result == SQLITE_OK) {
        result = iterator.next().invoke(db, api) { message ->
            errorMessage = message
        }.code
    }

    if (pzErrMsg != null && errorMessage != null) {
        val cString = errorMessage.cstr

        // Well, if malloc fails, there is nothing else to do, no exception throwing on capi
        native_sqlite3_malloc(cString.size)?.reinterpret<ByteVar>()?.let { pointer ->
            cString.place(pointer)
            pzErrMsg.pointed.value = pointer
        }
    }

    return result
}