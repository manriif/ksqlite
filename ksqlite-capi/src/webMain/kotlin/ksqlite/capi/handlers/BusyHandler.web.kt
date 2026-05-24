package ksqlite.capi.handlers

import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
        ),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer,
        count: Int,
    ): Int = handler(refPointer) { callback: Sqlite3BusyHandlerCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            count = count
        )
    }
}