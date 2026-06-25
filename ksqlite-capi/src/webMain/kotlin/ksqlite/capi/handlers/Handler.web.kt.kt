package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.stableRefDataHolder
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.JsReference
import kotlin.js.get

/**
 * Handler for native callback.
 */
internal abstract class Handler {

    /**
     * Manager owning the data associated to the reference pointer in [handle].
     */
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

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    companion object {

        /**
         * Returns [block]'s result, invoked with [Data] and optional appData obtained from a
         * previously referenced [refPointer].
         */
        protected inline fun <Ref : Handler, reified Data : Any, Result> JsReference<Ref>.handle(
            refPointer: WasmPointer,
            block: Ref.(data: Data, appData: Any?) -> Result
        ): Result = get().run {
            handle<Data, Result>(refPointer) { data, appData ->
                block(this, data, appData)
            }
        }
    }
}