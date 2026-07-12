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
package ksqlite.capi.handlers

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.stableRefDataHolder

/**
 * Returns [pointer] to a [CFunction] only if [data] is not `null`.
 */
internal fun <Fun : CFunction<*>, Pointer : CPointer<Fun>> callbackHandler(
    data: Any?,
    pointer: Pointer
): Pointer? {
    if (data == null) {
        return null
    }

    return pointer
}

/**
 * Returns [block]'s result, invoked with [Data] and optional application data obtained from a
 * previously referenced [refPointer].
 *
 * AppData type is erased to reduce complexity.
 */
internal inline fun <reified Data : Any, Result> handle(
    refPointer: COpaquePointer?,
    block: (data: Data, appData: Any?) -> Result
): Result = stableRefDataHolder<Data, Any?>(refPointer).run {
    block(data, appData)
}