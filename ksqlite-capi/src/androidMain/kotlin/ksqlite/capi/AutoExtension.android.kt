package ksqlite.capi

import ksqlite.OutputPointer
import ksqlite.callbacks.AutoExtensionCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines

/**
 * Singleton handler for auto extensions.
 */
internal val AutoExtensionHandler by lazy {
    AutoExtensionCallback(::autoExtensionHandler)
}

/**
 * Handler for [sqlite3_auto_extension].
 * Dispatches sqlite3_auto_extension call to all registered extensions.
 */
private fun autoExtensionHandler(
    dbPtr: Long,
    apiPtr: Long,
    outErrMsg: OutputPointer.OfString
): Int = autoExtensionHandle(
    db = sqlite3(dbPtr),
    api = sqlite3_api_routines(apiPtr),
    errorPointer = outErrMsg,
    setError = OutputPointer.OfString::value::set
)