package ksqlite.capi

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import ksqlite.SQLITE_OK
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.param
import ksqlite.capi.types.s3
import ksqlite.capi.types.s3_api_routines
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines
import ksqlite.sqlite3_malloc

/**
 * All registered [Sqlite3AutoExtensionCallback].
 */
internal val AutoExtensions = mutableListOf<Sqlite3AutoExtensionCallback>()

/**
 * Dispatches sqlite3_auto_extension call to all registered extensions.
 */
internal fun autoExtensionHandler(
    db: CPointer<s3>?,
    pzErrMsg: CPointer<CPointerVar<ByteVar>>?,
    pApi: CPointer<s3_api_routines>?
): Int {
    var result = SQLITE_OK
    var errorMessage: String? = null
    val dbParam = param { sqlite3(checkNotNull(db), true) }
    val apiParam = param { sqlite3_api_routines(checkNotNull(pApi), true) }
    val iterator = AutoExtensions.iterator()

    while (iterator.hasNext() && result == SQLITE_OK) {
        result = iterator.next().invoke(dbParam, apiParam) { message ->
            errorMessage = message
        }.code
    }

    if (pzErrMsg != null && errorMessage != null) {
        val cString = errorMessage.cstr

        // Well, if malloc fails, there is nothing else to do, no exception throwing on capi
        sqlite3_malloc(cString.size)?.reinterpret<ByteVar>()?.let { pointer ->
            cString.place(pointer)
            pzErrMsg.pointed.value = pointer
        }
    }

    return result
}