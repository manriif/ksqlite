package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback
import ksqlite.wasm.FunctionSignature
import ksqlite.wasm.WasmFunctions
import ksqlite.wasm.WasmPointer
import ksqlite.wasm.installFunction

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
internal class ProgressHandlerHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(FunctionSignature.Pointer),
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer
    ): Int = handle(refPointer) { callback: Sqlite3ProgressHandlerCallback<Any?>, appData ->
        callback.apply(appData)
    }
}