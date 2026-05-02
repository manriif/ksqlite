package ksqlite.capi.handlers

import ksqlite.capi.convertResult
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines

/**
 * All registered [Sqlite3AutoExtensionCallback].
 */
internal val AutoExtensions = mutableListOf<Sqlite3AutoExtensionCallback>()

/**
 * Registers the auto extension [callback].
 * The [invoke] block must return the result of [ksqlite.capi.sqlite3_auto_extension].
 */
internal fun autoExtensionRegister(
    callback: Sqlite3AutoExtensionCallback,
    invoke: () -> Int
): Sqlite3Result {
    var result: Sqlite3Result = Sqlite3Result.OK

    if (AutoExtensions.isEmpty()) {
        result = convertResult(invoke())
    }

    if (result == Sqlite3Result.OK) {
        AutoExtensions.add(callback)
    }

    return result
}

/**
 * Unregisters the auto extension [callback].
 * The [invoke] block must return the result of [ksqlite.capi.sqlite3_cancel_auto_extension].
 */
internal fun autoExtensionUnregister(
    callback: Sqlite3AutoExtensionCallback,
    invoke: () -> Int
): Int {
    if (!AutoExtensions.remove(callback)) {
        return 0
    }

    if (AutoExtensions.isEmpty()) {
        val _ = invoke()
    }

    return 1
}

/**
 * Handler for [ksqlite.capi.sqlite3_auto_extension].
 * Dispatches sqlite3_auto_extension call to all registered extensions.
 */
internal fun <Pointer> autoExtensionHandle(
    db: sqlite3,
    api: sqlite3_api_routines,
    errorPointer: Pointer?,
    setError: (pointer: Pointer, message: String) -> Unit
): Int {
    var result: Sqlite3Result = Sqlite3Result.OK
    val iterator = AutoExtensions.iterator()
    var errorMessage: String? = null

    while (iterator.hasNext() && result == Sqlite3Result.OK) {
        result = iterator.next().invoke(db, api) { message ->
            errorMessage = message
        }
    }

    if (errorPointer != null && errorMessage != null) {
        setError(errorPointer, errorMessage)
    }

    return result.code
}