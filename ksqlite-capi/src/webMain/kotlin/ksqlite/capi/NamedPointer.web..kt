package ksqlite.capi

import ksqlite.capi.interop.wasm.WasmMemory
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.allocCString
import ksqlite.capi.callbacks.Sqlite3DestructorCallback

/**
 * Wrapper for [sqlite3_bind_pointer] and [sqlite3_result_pointer] userData.
 */
internal class NamedPointer(
    val typePointer: WasmPointer?,
    private val memory: WasmMemory,
    private val destructor: Sqlite3DestructorCallback?
) {
    /**
     * Destructor replacing original user provided destructor.
     */
    val disposer: Sqlite3DestructorCallback = { userData ->
        destructor?.invoke(userData)
        typePointer?.let(memory::dealloc)
    }
}

/**
 * Returns a [NamedPointer] which allocates memory for [type] if not null.
 *
 * The returned [NamedPointer.disposer] must be used in place of [destructor] in order to clear
 * the allocated memory.
 */
internal inline fun <R> allocateNamedPointer(
    type: String?,
    noinline destructor: Sqlite3DestructorCallback?,
    block: NamedPointer.() -> R
): R {
    val memory = wasm
    val typePointer = type?.let(memory::allocCString)

    return block(
        NamedPointer(
            typePointer = typePointer,
            memory = memory,
            destructor = destructor
        )
    )
}