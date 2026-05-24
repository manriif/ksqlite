package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.interop.wasm.WasmMemory
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.allocCString

/**
 * Wrapper for [sqlite3_bind_pointer] and [sqlite3_result_pointer] userData.
 */
internal class NamedPointer<Data>(
    val name: WasmPointer?,
    private val memory: WasmMemory,
    private val destroy: Sqlite3DestroyCallback<Data>?
) {

    /**
     * Invokes application [destroy] and free [name] allocated memory.
     */
    fun destroy(data: Data) {
        destroy?.handle(data)
        name?.let(memory::dealloc)
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
    destroy: Sqlite3DestroyCallback<Data>?,
    block: (
        ptr: NamedPointer<Data>,
        ptrDestroy: Sqlite3DestroyCallback<Data>
    ) -> R
): R {
    val memory = wasm
    val typePointer = name?.let(memory::allocCString)

    val pointer = NamedPointer(
        name = typePointer,
        memory = memory,
        destroy = destroy
    )

    return block(pointer) { pointer.destroy(it) }
}