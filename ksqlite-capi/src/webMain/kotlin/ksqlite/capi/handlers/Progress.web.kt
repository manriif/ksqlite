package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction

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