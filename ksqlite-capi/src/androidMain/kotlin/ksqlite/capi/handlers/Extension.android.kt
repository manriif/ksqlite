package ksqlite.capi.handlers

import ksqlite.AutoExtensionCallback
import ksqlite.KsqliteJniException
import ksqlite.capi.autoExtensionHandle
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines

/**
 * Singleton handler for auto extensions.
 */
internal val SharedAutoExtensionHandler by lazy {
    AutoExtensionCallback(::autoExtensionHandler)
}

/**
 * Handler for [ksqlite.capi.sqlite3_auto_extension].
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