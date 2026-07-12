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
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.orNull
import ksqlite.foreign.wasm.WasmMemory
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.allocCString

/**
 * Wrapper for [sqlite3_bind_pointer] and [sqlite3_result_pointer] userData.
 */
internal class NamedPointer<Data>(
    val name: WasmPointer,
    private val memory: WasmMemory,
    private val destroy: SqliteDestroyCallback<Data>?
) {

    /**
     * Invokes application [destroy] and free [name] allocated memory.
     */
    fun destroy(data: Data) {
        destroy?.apply(data)
        name.orNull?.let(memory::dealloc)
    }
}

/**
 * Returns a [NamedPointer] which allocates memory for [name] if not null.
 *
 * The destructor passed to [block] must be used in place of [destroy] in order to free the
 * allocated memory.
 */
internal inline fun <Data, R> allocateNamedPointer(
    name: String?,
    destroy: SqliteDestroyCallback<Data>?,
    memory: WasmMemory = wasm,
    block: (
        ptr: NamedPointer<Data>,
        ptrDestroy: SqliteDestroyCallback<Data>
    ) -> R
): R {
    val typePointer = name?.let(memory::allocCString)

    val pointer = NamedPointer(
        name = typePointer.notNull,
        memory = memory,
        destroy = destroy
    )

    return block(pointer) { pointer.destroy(it) }
}