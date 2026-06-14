package ksqlite.capi

import ksqlite.foreign.OutputPointer
import ksqlite.foreign.callbacks.AutoExtensionCallback
import ksqlite.capi.types.sqlite3

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
    api = apiPtr,
    errorPointer = outErrMsg,
    setError = OutputPointer.OfString::value::set
)