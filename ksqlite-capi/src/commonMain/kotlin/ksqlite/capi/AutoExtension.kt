/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.capi

import ksqlite.capi.callbacks.AutoExtensionCallbackScope
import ksqlite.capi.callbacks.AutoExtensionFailureResult
import ksqlite.capi.callbacks.AutoExtensionSuccessResult
import ksqlite.capi.callbacks.SqliteAutoExtensionCallback
import ksqlite.types.SqliteResultCode
import ksqlite.types.internal.convertResultCode

/**
 * All registered [SqliteAutoExtensionCallback].
 */
private val AutoExtensions = mutableListOf<SqliteAutoExtensionCallback>()

/**
 * Registers the auto extension [callback].
 * The [invoke] block must return the result of [sqlite3_auto_extension].
 */
internal fun autoExtensionRegister(
    callback: SqliteAutoExtensionCallback,
    invoke: () -> Int
): SqliteResultCode {
    var result: SqliteResultCode = SqliteResultCode.OK

    if (AutoExtensions.isEmpty()) {
        result = convertResultCode(invoke())
    }

    if (result == SqliteResultCode.OK) {
        AutoExtensions.add(callback)
    }

    return result
}

/**
 * Unregisters the auto extension [callback].
 * The [invoke] block must return the result of [sqlite3_cancel_auto_extension].
 */
internal fun autoExtensionUnregister(
    callback: SqliteAutoExtensionCallback,
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
    var result: SqliteResultCode = SqliteResultCode.OK

    if (!iterator.hasNext()) {
        return result.code
    }

    var errorMessage: String? = null

    while (iterator.hasNext() && result == SqliteResultCode.OK) {
        result = iterator.next().run {
            when (val result = AutoExtensionCallbackScope.apply(db)) {
                AutoExtensionSuccessResult -> SqliteResultCode.OK

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