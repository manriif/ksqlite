package ksqlite.capi.handlers

import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.getReferencedData
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Handler for native callback.
 */
internal abstract class Handler(protected val manager: MemoryManager) {

    /**
     * Installs the wasm to js function.
     */
    abstract fun WasmFunctions.install(): WasmPointer

    /**
     * Returns [block]'s result, invoked with [Data] and optional userData obtained from a
     * previously referenced [refPointer].
     */
    protected inline fun <reified Data : Any, Result> handle(
        refPointer: WasmPointer,
        block: (data: Data, userData: sqlite3_mutable_pointer?) -> Result
    ): Result {
        val (data, userData) = manager
            .getStableRef(refPointer)
            .getReferencedData<Data>()

        return block(data, userData)
    }
}