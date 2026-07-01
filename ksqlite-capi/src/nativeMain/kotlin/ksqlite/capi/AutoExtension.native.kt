package ksqlite.capi

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import ksqlite.foreign.sqlite3_mprintf

/**
 * Static C function for [autoExtensionHandler].
 */
internal val AutoExtensionHandler = staticCFunction(::autoExtensionHandler)

/**
 * Handler for [sqlite3_auto_extension].
 */
private fun autoExtensionHandler(
    db: CPointer<s3>?,
    pzErrMsg: CPointer<CPointerVar<ByteVar>>?,
    pApi: CPointer<s3_api>?
) = autoExtensionHandle(
    db = sqlite3(db!!),
    api = pApi,
    errorPointer = pzErrMsg
) { errorPointer, message ->
    errorPointer.pointed.value = sqlite3_mprintf(message)
}