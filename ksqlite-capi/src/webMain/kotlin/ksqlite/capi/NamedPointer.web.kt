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