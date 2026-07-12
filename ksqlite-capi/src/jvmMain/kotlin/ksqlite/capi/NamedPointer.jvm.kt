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

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.notNull
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Wrapper for [sqlite3_bind_pointer] and [sqlite3_result_pointer] data.
 */
internal class NamedPointer<Data>(
    val name: MemorySegment,
    private val arena: Arena?,
    private val destroy: SqliteDestroyCallback<Data>?
) {

    /**
     * Invokes application [destroy] and closes the associated [arena].
     */
    fun destroy(data: Data) {
        destroy?.apply(data)
        arena?.close()
    }
}

/**
 * Returns a [NamedPointer] which allocates memory for [name] if not null.
 *
 * The destructor passed to [block] must be used in place of [destroy] in order to close the
 * associated [Arena].
 */
internal inline fun <Data, R> allocateNamedPointer(
    name: String?,
    destroy: SqliteDestroyCallback<Data>?,
    block: (
        ptr: NamedPointer<Data>,
        ptrDestroy: SqliteDestroyCallback<Data>
    ) -> R
): R {
    val arena = name?.let { Arena.ofShared() }
    val typePointer = arena?.run { name.allocateUtf8() }

    val pointer = NamedPointer(
        name = typePointer.notNull,
        arena = arena,
        destroy = destroy
    )

    return block(pointer) { pointer.destroy(it) }
}