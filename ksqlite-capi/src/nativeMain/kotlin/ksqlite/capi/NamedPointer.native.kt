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

import kotlinx.cinterop.Arena
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.cstr
import ksqlite.capi.callbacks.SqliteDestroyCallback

/**
 * Wrapper for [sqlite3_bind_pointer] and [sqlite3_result_pointer] data.
 */
internal class NamedPointer<Data>(
    val name: CPointer<ByteVar>?,
    private val arena: Arena?,
    private val destroy: SqliteDestroyCallback<Data>?
) {

    /**
     * Invokes application [destroy] and clears the associated [arena].
     */
    fun destroy(data: Data) {
        destroy?.apply(data)
        arena?.clear()
    }
}

/**
 * Returns a [NamedPointer] which allocates memory for [type] if not null.
 *
 * The destructor passed to [block] must be used in place of [destroy] in order to clear the
 * associated [Arena].
 */
internal inline fun <Data, R> allocateNamedPointer(
    type: String?,
    destroy: SqliteDestroyCallback<Data>?,
    block: (
        ptr: NamedPointer<Data>,
        ptrDestroy: SqliteDestroyCallback<Data>
    ) -> R
): R {
    val arena = type?.let { Arena() }
    val typePointer = arena?.let(type.cstr::getPointer)

    val pointer = NamedPointer(
        name = typePointer,
        arena = arena,
        destroy = destroy
    )

    return block(pointer) { pointer.destroy(it) }
}