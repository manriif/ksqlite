package ksqlite.capi.handlers

import ksqlite.wasm.FunctionSignature
import ksqlite.wasm.WasmFunctions
import ksqlite.wasm.WasmPointer
import ksqlite.wasm.installFunction
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
        ),
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer,
        count: Int,
    ): Int = handle(refPointer) { callback: Sqlite3BusyHandlerCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            count = count
        )
    }
}