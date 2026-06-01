package ksqlite.capi.handlers

import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.stableRefDataHolder

/**
 * Handler for native callback.
 */
internal abstract class Handler {

    lateinit var manager: MemoryManager

    /**
     * Installs the wasm to js function.
     */
    abstract fun install(functions: WasmFunctions): WasmPointer

    /**
     * Returns [block]'s result, invoked with [Data] and optional appData obtained from a
     * previously referenced [refPointer].
     */
    protected inline fun <reified Data : Any, Result> handle(
        refPointer: WasmPointer,
        block: (data: Data, appData: Any?) -> Result
    ): Result = manager.stableRefDataHolder<Data, Any?>(refPointer).run {
        block(data, appData)
    }
}