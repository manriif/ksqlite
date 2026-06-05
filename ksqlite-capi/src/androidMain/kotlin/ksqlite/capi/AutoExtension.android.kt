package ksqlite.capi

import ksqlite.callbacks.AutoExtensionCallback
import ksqlite.KsqliteJniException
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
    apiPtr: Long
): Int {
    var errorMessage: String? = null

    val resultCode = autoExtensionHandle(
        db = sqlite3(dbPtr),
        api = sqlite3_api_routines(apiPtr),
        errorPointer = 0
    ) { _, message ->
        errorMessage = message
    }

    errorMessage?.let { message ->
        // Exception is handled on JNI side
        throw KsqliteJniException(resultCode, message)
    }

    return resultCode
}