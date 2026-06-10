package ksqlite.capi

import ksqlite.capi.callbacks.AutoExtensionCallbackScope
import ksqlite.capi.callbacks.AutoExtensionFailureResult
import ksqlite.capi.callbacks.AutoExtensionSuccessResult
import ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3

/**
 * All registered [Sqlite3AutoExtensionCallback].
 */
private val AutoExtensions = mutableListOf<Sqlite3AutoExtensionCallback>()

/**
 * Registers the auto extension [callback].
 * The [invoke] block must return the result of [sqlite3_auto_extension].
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
 * The [invoke] block must return the result of [sqlite3_cancel_auto_extension].
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
 * Resets the registered extensions.
 * The [invoke] block must execute the result of [sqlite3_reset_auto_extension].
 */
internal inline fun autoExtensionReset(invoke: () -> Unit) {
    AutoExtensions.clear()
    invoke()
}

/**
 * Handles the  for [sqlite3_auto_extension].
 * Dispatches sqlite3_auto_extension call to all registered extensions.
 */
internal fun <Pointer> autoExtensionHandle(
    db: sqlite3,
    @Suppress("unused") api: Any?,
    errorPointer: Pointer?,
    setError: (pointer: Pointer, message: String) -> Unit
): Int {
    val iterator = AutoExtensions.iterator()
    var result: Sqlite3Result = Sqlite3Result.OK

    if (!iterator.hasNext()) {
        return result.code
    }

    var errorMessage: String? = null

    while (iterator.hasNext() && result == Sqlite3Result.OK) {
        result = iterator.next().run {
            when (val result = AutoExtensionCallbackScope.apply(db)) {
                AutoExtensionSuccessResult -> Sqlite3Result.OK

                is AutoExtensionFailureResult -> result.result.also {
                    errorMessage = result.message
                }
            }
        }
    }

    if (errorPointer != null && errorMessage != null) {
        setError(errorPointer, errorMessage)
    }

    return result.code
}