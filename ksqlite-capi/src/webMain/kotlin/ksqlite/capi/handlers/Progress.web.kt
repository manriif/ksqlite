package ksqlite.capi.handlers

import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
internal class ProgressHandlerHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(FunctionSignature.Pointer),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer
    ): Int = handler(refPointer) { callback: Sqlite3ProgressHandlerCallback<Any?>, appData ->
        callback.handle(appData)
    }
}